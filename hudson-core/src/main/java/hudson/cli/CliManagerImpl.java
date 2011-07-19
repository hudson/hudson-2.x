/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *        
 *
 *******************************************************************************/ 

package hudson.cli;

import hudson.remoting.Channel;
import hudson.model.Hudson;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.classes.DiscoverClasses;
import org.apache.commons.discovery.resource.names.DiscoverServiceNames;
import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.commons.discovery.ResourceClassIterator;
import org.eclipse.hudson.cli.CliEntryPoint;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.CmdLineParser;
import org.jvnet.tiger_types.Types;

import java.util.List;
import java.util.Locale;
import java.util.Collections;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;

/**
 * {@link CliEntryPoint} implementation exposed to the remote CLI.
 *
 * @author Kohsuke Kawaguchi
 */
public class CliManagerImpl implements CliEntryPoint, Serializable {
    public CliManagerImpl() {
    }

    public int main(List<String> args, Locale locale, InputStream stdin, OutputStream stdout, OutputStream stderr) {
        // remoting sets the context classloader to the RemoteClassLoader,
        // which slows down the classloading. we don't load anything from CLI,
        // so counter that effect.
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        PrintStream out = new PrintStream(stdout);
        PrintStream err = new PrintStream(stderr);

        String subCmd = args.get(0);
        CLICommand cmd = CLICommand.clone(subCmd);
        if(cmd!=null) {
            final CLICommand old = CLICommand.setCurrent(cmd);
            try {
                return cmd.main(args.subList(1,args.size()),locale, stdin, out, err);
            } finally {
                CLICommand.setCurrent(old);
            }
        }

        err.println("No such command: "+subCmd);
        new HelpCommand().main(Collections.<String>emptyList(), locale, stdin, out, err);
        return -1;
    }

    public boolean hasCommand(String name) {
        return CLICommand.clone(name)!=null;
    }

    public int protocolVersion() {
        return VERSION;
    }

    private Object writeReplace() {
        return Channel.current().export(CliEntryPoint.class,this);
    }

    static {
        // register option handlers that are defined
        ClassLoaders cls = new ClassLoaders();
        cls.put(Hudson.getInstance().getPluginManager().uberClassLoader);

        ResourceNameIterator servicesIter =
            new DiscoverServiceNames(cls).findResourceNames(OptionHandler.class.getName());
        final ResourceClassIterator itr =
            new DiscoverClasses(cls).findResourceClasses(servicesIter);

        while(itr.hasNext()) {
            Class h = itr.nextResourceClass().loadClass();
            Class c = Types.erasure(Types.getTypeArgument(Types.getBaseClass(h, OptionHandler.class), 0));
            CmdLineParser.registerHandler(c,h);
        }
    }
}
