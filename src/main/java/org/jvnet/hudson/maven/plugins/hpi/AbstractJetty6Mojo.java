package org.jvnet.hudson.maven.plugins.hpi;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.plugin.Jetty6PluginServer;
import org.mortbay.jetty.plugin.util.JettyPluginServer;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.xml.XmlConfiguration;

import java.io.File;

/**
 * DO NOT MODIFY.
 *
 * Copied verbatim from Jetty code, just so that Maven's qdox can find
 * all the injection points. All the changes should go to {@link RunMojo}.
 */
public abstract class AbstractJetty6Mojo extends AbstractJettyMojo {

    /**
     * List of connectors to use. If none are configured
     * then we use a single SelectChannelConnector at port 8080
     *
     * @parameter
     */
    private Connector[] connectors;


    /**
     * List of security realms to set up. Optional.
     * @parameter
     */
    private UserRealm[] userRealms;



    /**
     * A RequestLog implementation to use for the webapp at runtime.
     * Optional.
     * @parameter
     */
    private RequestLog requestLog;



    /**
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#getConfiguredUserRealms()
     */
    public Object[] getConfiguredUserRealms()
    {
        return this.userRealms;
    }

    /**
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#getConfiguredConnectors()
     */
    public Object[] getConfiguredConnectors()
    {
        return this.connectors;
    }


    public Object getConfiguredRequestLog()
    {
        return this.requestLog;
    }


    public void applyJettyXml() throws Exception
    {

        if (getJettyXmlFileName() == null)
            return;

        getLog().info( "Configuring Jetty from xml configuration file = " + getJettyXmlFileName() );
        File f = new File (getJettyXmlFileName());
        XmlConfiguration xmlConfiguration = new XmlConfiguration(f.toURL());
        xmlConfiguration.configure(getServer().getProxiedObject());
    }


    /**
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#createServer()
     */
    public JettyPluginServer createServer() throws Exception
    {
        return new Jetty6PluginServer();
    }

}
