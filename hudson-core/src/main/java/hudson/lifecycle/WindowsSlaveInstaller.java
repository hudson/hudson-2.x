/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Inc., Kohsuke Kawaguchi, Winston Prakash
 *     
 *
 *******************************************************************************/ 

package hudson.lifecycle;

import hudson.Launcher.LocalLauncher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.Engine;
import hudson.remoting.jnlp.MainDialog;
import hudson.remoting.jnlp.MainMenu;
import hudson.util.StreamTaskListener;
import hudson.util.jna.NativeAccessException;
import hudson.util.jna.NativeUtils;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.logging.Level;

import static javax.swing.JOptionPane.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class WindowsSlaveInstaller implements Callable<Void,RuntimeException>, ActionListener {
    /**
     * Root directory of this slave.
     * String, not File because the platform can be different.
     */
    private final String rootDir;

    private transient Engine engine;
    private transient MainDialog dialog;

    public WindowsSlaveInstaller(String rootDir) {
        this.rootDir = rootDir;
    }

    public Void call() {
        if(File.separatorChar=='/') return null;    // not Windows
        if(System.getProperty("hudson.showWindowsServiceInstallLink")==null)
            return null;    // only show this when it makes sense, which is when we run from JNLP

        dialog = MainDialog.get();
        if(dialog==null)     return null;    // can't find the main window. Maybe not running with GUI

        // capture the engine
        engine = Engine.current();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainMenu mainMenu = dialog.getMainMenu();
                JMenu m = mainMenu.getFileMenu();
                JMenuItem menu = new JMenuItem(Messages.WindowsInstallerLink_DisplayName(), KeyEvent.VK_W);
                menu.addActionListener(WindowsSlaveInstaller.this);
                m.add(menu);
                mainMenu.commit();
            }
        });

        return null;
    }

    /**
     * Invokes slave.exe with a SCM management command.
     *
     * <p>
     * If it fails in a way that indicates the presence of UAC, retry in an UAC compatible manner.
     */
    static int runElevated(File slaveExe, String command, TaskListener out, File pwd) throws IOException, InterruptedException {
        try {
            return new LocalLauncher(out).launch().cmds(slaveExe, command).stdout(out).pwd(pwd).join();
        } catch (IOException e) {
            if (e.getMessage().contains("CreateProcess") && e.getMessage().contains("=740")) {
                // fall through
            } else {
                throw e;
            }
        }

        String logFile = "redirect.log";
        try {
            return NativeUtils.getInstance().windowsExec(slaveExe, command, logFile, pwd);
        } catch (NativeAccessException ex) {
            Logger.getLogger(WindowsSlaveInstaller.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            FileInputStream fin = new FileInputStream(new File(pwd, "redirect.log"));
            IOUtils.copy(fin, out.getLogger());
            fin.close();
        }
    }

    /**
     * Called when the install menu is selected
     */
    public void actionPerformed(ActionEvent e) {
        try {
            int r = JOptionPane.showConfirmDialog(dialog,
                    Messages.WindowsSlaveInstaller_ConfirmInstallation(),
                    Messages.WindowsInstallerLink_DisplayName(), OK_CANCEL_OPTION);
            if(r!=JOptionPane.OK_OPTION)    return;

            if(!NativeUtils.getInstance().isDotNetInstalled(2, 0)) {
                JOptionPane.showMessageDialog(dialog,Messages.WindowsSlaveInstaller_DotNetRequired(),
                        Messages.WindowsInstallerLink_DisplayName(), ERROR_MESSAGE);
                return;
            }

            final File dir = new File(rootDir);
            if (!dir.exists()) {
                JOptionPane.showMessageDialog(dialog,Messages.WindowsSlaveInstaller_RootFsDoesntExist(rootDir),
                        Messages.WindowsInstallerLink_DisplayName(), ERROR_MESSAGE);
                return;
            }

            final File slaveExe = new File(dir, "hudson-slave.exe");
            FileUtils.copyURLToFile(getClass().getResource("/windows-service/hudson.exe"), slaveExe);

            // write out the descriptor
            URL jnlp = new URL(engine.getHudsonUrl(),"computer/"+Util.rawEncode(engine.slaveName)+"/slave-agent.jnlp");
            String xml = generateSlaveXml(
                    generateServiceId(rootDir),
                    System.getProperty("java.home")+"\\bin\\java.exe", "-jnlpUrl "+jnlp.toExternalForm());
            FileUtils.writeStringToFile(new File(dir, "hudson-slave.xml"),xml,"UTF-8");

            // copy slave.jar
            URL slaveJar = new URL(engine.getHudsonUrl(),"jnlpJars/remoting.jar");
            File dstSlaveJar = new File(dir,"slave.jar").getCanonicalFile();
            if(!dstSlaveJar.exists()) // perhaps slave.jar is already there?
                FileUtils.copyURLToFile(slaveJar,dstSlaveJar);

            // install as a service
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamTaskListener task = new StreamTaskListener(baos);
            r = runElevated(slaveExe,"install",task,dir);
            if(r!=0) {
                JOptionPane.showMessageDialog(
                    dialog,baos.toString(),"Error", ERROR_MESSAGE);
                return;
            }

            r = JOptionPane.showConfirmDialog(dialog,
                    Messages.WindowsSlaveInstaller_InstallationSuccessful(),
                    Messages.WindowsInstallerLink_DisplayName(), OK_CANCEL_OPTION);
            if(r!=JOptionPane.OK_OPTION)    return;

            // let the service start after we close our connection, to avoid conflicts
            Runtime.getRuntime().addShutdownHook(new Thread("service starter") {
                public void run() {
                    try {
                        StreamTaskListener task = StreamTaskListener.fromStdout();
                        int r = runElevated(slaveExe,"start",task,dir);
                        task.getLogger().println(r==0?"Successfully started":"start service failed. Exit code="+r);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            System.exit(0);
        } catch (Exception t) {// this runs as a JNLP app, so if we let an exeption go, we'll never find out why it failed 
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            JOptionPane.showMessageDialog(dialog,sw.toString(),"Error", ERROR_MESSAGE);
        }
    }

    public static String generateServiceId(String slaveRoot) throws IOException {
        return "hudsonslave-"+slaveRoot.replace(':','_').replace('\\','_').replace('/','_');
    }

    public static String generateSlaveXml(String id, String java, String args) throws IOException {
        String xml = IOUtils.toString(WindowsSlaveInstaller.class.getResourceAsStream("/windows-service/hudson-slave.xml"), "UTF-8");
        xml = xml.replace("@ID@", id);
        xml = xml.replace("@JAVA@", java);
        xml = xml.replace("@ARGS@", args);
        return xml;
    }

    private static final long serialVersionUID = 1L;
}
