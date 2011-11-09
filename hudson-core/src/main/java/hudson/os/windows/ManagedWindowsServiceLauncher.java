/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.os.windows;

import hudson.Extension;
import hudson.lifecycle.WindowsSlaveInstaller;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.Channel.Listener;
import hudson.remoting.SocketInputStream;
import hudson.remoting.SocketOutputStream;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import hudson.util.IOUtils;
import hudson.util.Secret;
import hudson.util.jna.DotNet;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JISession;
import org.jvnet.hudson.remcom.WindowsRemoteProcessLauncher;
import org.jvnet.hudson.wmi.SWbemServices;
import org.jvnet.hudson.wmi.WMI;
import org.jvnet.hudson.wmi.Win32Service;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hudson.Util.copyStreamAndClose;
import static org.jvnet.hudson.wmi.Win32Service.Win32OwnProcess;

/**
 * Windows slave installed/managed as a service entirely remotely
 *
 * @author Kohsuke Kawaguchi
 */
public class ManagedWindowsServiceLauncher extends ComputerLauncher {
    /**
     * "[DOMAIN\\]USERNAME" to follow the Windows convention.
     */
    //TODO: review and check whether we can do it private
    public final String userName;

    //TODO: review and check whether we can do it private
    public final Secret password;

    public String getUserName() {
        return userName;
    }

    public Secret getPassword() {
        return password;
    }

    @DataBoundConstructor
    public ManagedWindowsServiceLauncher(String userName, String password) {
        this.userName = userName;
        this.password = Secret.fromString(password);
    }

    private JIDefaultAuthInfoImpl createAuth() {
        String[] tokens = userName.split("\\\\");
        if(tokens.length==2)
            return new JIDefaultAuthInfoImpl(tokens[0], tokens[1], Secret.toString(password));
        return new JIDefaultAuthInfoImpl("", userName, Secret.toString(password));
    }

    private NtlmPasswordAuthentication createSmbAuth() {
        JIDefaultAuthInfoImpl auth = createAuth();
        return new NtlmPasswordAuthentication(auth.getDomain(), auth.getUserName(), auth.getPassword());
    }

    @Override
    public void launch(final SlaveComputer computer, final TaskListener listener) throws IOException, InterruptedException {
        try {
            final PrintStream logger = listener.getLogger();
            final String name = determineHost(computer);

            logger.println(Messages.ManagedWindowsServiceLauncher_ConnectingTo(name));

            InetAddress host = InetAddress.getByName(name);

            /*
                Somehow this didn't work for me, so I'm disabling it.
             */
            // ping check
//            if (!host.isReachable(3000)) {
//                logger.println("Failed to ping "+name+". Is this a valid reachable host name?");
//                // continue anyway, just in case it's just ICMP that's getting filtered
//            }

            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(host,135),5000);
                s.close();
            } catch (IOException e) {
                logger.println("Failed to connect to port 135 of "+name+". Is Windows firewall blocking this port? Or did you disable DCOM service?");
                // again, let it continue.
            }

            JIDefaultAuthInfoImpl auth = createAuth();
            JISession session = JISession.createSession(auth);
            session.setGlobalSocketTimeout(60000);
            SWbemServices services = WMI.connect(session, name);


            String path = computer.getNode().getRemoteFS();
            if (path.indexOf(':')==-1)   throw new IOException("Remote file system root path of the slave needs to be absolute: "+path);
            SmbFile remoteRoot = new SmbFile("smb://" + name + "/" + path.replace('\\', '/').replace(':', '$')+"/",createSmbAuth());

            if(!remoteRoot.exists())
                remoteRoot.mkdirs();

            try {// does Java exist?
                logger.println("Checking if Java exists");
                WindowsRemoteProcessLauncher wrpl = new WindowsRemoteProcessLauncher(name,auth);
                Process proc = wrpl.launch("java -fullversion","c:\\");
                proc.getOutputStream().close();
                IOUtils.copy(proc.getInputStream(),logger);
                proc.getInputStream().close();
                int exitCode = proc.waitFor();
                if (exitCode==1) {// we'll get this error code if Java is not found
                    //TODO enable me when JDK installer based on REST API will be ready
                    logger.println("No JDK found on slave node. Please install JDK");
                    throw new InterruptedException("No JDK found on slave node. Please install JDK");
//                    logger.println("No Java found. Downloading JDK");
//                    JDKInstaller jdki = new JDKInstaller("jdk-6u16-oth-JPR@CDS-CDS_Developer",true);
//                    URL jdk = jdki.locate(listener, Platform.WINDOWS, CPU.i386);
//
//                    listener.getLogger().println("Installing JDK");
//                    copyStreamAndClose(jdk.openStream(), new SmbFile(remoteRoot, "jdk.exe").getOutputStream());
//
//                    String javaDir = path + "\\jdk"; // this is where we install Java to
//
//                    WindowsRemoteFileSystem fs = new WindowsRemoteFileSystem(name, createSmbAuth());
//                    fs.mkdirs(javaDir);
//
//                    jdki.install(new WindowsRemoteLauncher(listener,wrpl), Platform.WINDOWS,
//                            fs, listener, javaDir ,path+"\\jdk.exe");
                }
            } catch (Exception e) {
                e.printStackTrace(listener.error("Failed to prepare Java"));
            }

            String id = WindowsSlaveInstaller.generateServiceId(path);
            Win32Service slaveService = services.getService(id);
            if(slaveService==null) {
                logger.println(Messages.ManagedWindowsServiceLauncher_InstallingSlaveService());
                if(!DotNet.isInstalled(2,0, name, auth)) {
                    // abort the launch
                    logger.println(Messages.ManagedWindowsServiceLauncher_DotNetRequired());
                    return;
                }

                // copy exe
                logger.println(Messages.ManagedWindowsServiceLauncher_CopyingSlaveExe());
                copyStreamAndClose(getClass().getResource("/windows-service/hudson.exe").openStream(), new SmbFile(remoteRoot,"hudson-slave.exe").getOutputStream());

                copySlaveJar(logger, remoteRoot);

                // copy hudson-slave.xml
                logger.println(Messages.ManagedWindowsServiceLauncher_CopyingSlaveXml());
                String xml = WindowsSlaveInstaller.generateSlaveXml(id,"javaw.exe","-tcp %BASE%\\port.txt");
                copyStreamAndClose(new ByteArrayInputStream(xml.getBytes("UTF-8")), new SmbFile(remoteRoot,"hudson-slave.xml").getOutputStream());

                // install it as a service
                logger.println(Messages.ManagedWindowsServiceLauncher_RegisteringService());
                Document dom = new SAXReader().read(new StringReader(xml));
                Win32Service svc = services.Get("Win32_Service").cast(Win32Service.class);
                int r = svc.Create(
                        id,
                        dom.selectSingleNode("/service/name").getText()+" at "+path,
                        path+"\\hudson-slave.exe",
                        Win32OwnProcess, 0, "Manual", true);
                if(r!=0) {
                    listener.error("Failed to create a service: "+svc.getErrorMessage(r));
                    return;
                }
                slaveService = services.getService(id);
            } else {
                copySlaveJar(logger, remoteRoot);                
            }

            logger.println(Messages.ManagedWindowsServiceLauncher_StartingService());
            slaveService.start();

            // wait until we see the port.txt, but don't do so forever
            logger.println(Messages.ManagedWindowsServiceLauncher_WaitingForService());
            SmbFile portFile = new SmbFile(remoteRoot, "port.txt");
            for( int i=0; !portFile.exists(); i++ ) {
                if(i>=30) {
                    listener.error(Messages.ManagedWindowsServiceLauncher_ServiceDidntRespond());
                    return;
                }
                Thread.sleep(1000);
            }
            int p = readSmbFile(portFile);

            // connect
            logger.println(Messages.ManagedWindowsServiceLauncher_ConnectingToPort(p));
            final Socket s = new Socket(name,p);

            // ready
            computer.setChannel(new BufferedInputStream(new SocketInputStream(s)),
                new BufferedOutputStream(new SocketOutputStream(s)),
                listener.getLogger(),new Listener() {
                    @Override
                    public void onClosed(Channel channel, IOException cause) {
                        afterDisconnect(computer,listener);
                    }
                });
        } catch (SmbException e) {
            e.printStackTrace(listener.error(e.getMessage()));
        } catch (JIException e) {
            if(e.getErrorCode()==5)
                // access denied error
                e.printStackTrace(listener.error(Messages.ManagedWindowsServiceLauncher_AccessDenied()));
            else
                e.printStackTrace(listener.error(e.getMessage()));
        } catch (DocumentException e) {
            e.printStackTrace(listener.error(e.getMessage()));
        }
    }

    /**
     * Determines the host name (or the IP address) to connect to.
     */
    protected String determineHost(Computer c) throws IOException, InterruptedException {
        return c.getName();
    }

    private void copySlaveJar(PrintStream logger, SmbFile remoteRoot) throws IOException {
        // copy slave.jar
        logger.println("Copying slave.jar");
        copyStreamAndClose(Hudson.getInstance().getJnlpJars("slave.jar").getURL().openStream(), new SmbFile(remoteRoot,"slave.jar").getOutputStream());
    }

    private int readSmbFile(SmbFile f) throws IOException {
        InputStream in=null;
        try {
            in = f.getInputStream();
            return Integer.parseInt(IOUtils.toString(in));
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
        try {
            JIDefaultAuthInfoImpl auth = createAuth();
            JISession session = JISession.createSession(auth);
            session.setGlobalSocketTimeout(60000);
            SWbemServices services = WMI.connect(session, computer.getName());
            Win32Service slaveService = services.getService("hudsonslave");
            if(slaveService!=null) {
                listener.getLogger().println(Messages.ManagedWindowsServiceLauncher_StoppingService());
                slaveService.StopService();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace(listener.error(e.getMessage()));
        } catch (JIException e) {
            e.printStackTrace(listener.error(e.getMessage()));
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerLauncher> {
        public String getDisplayName() {
            return Messages.ManagedWindowsServiceLauncher_DisplayName();
        }
    }

    static {
        Logger.getLogger("org.jinterop").setLevel(Level.WARNING);
    }
}
