/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

import hudson.Extension;
import hudson.model.Hudson;
import hudson.remoting.ChannelClosedException;
import groovy.lang.Binding;
import groovy.lang.Closure;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.codehaus.groovy.tools.shell.Shell;
import org.codehaus.groovy.tools.shell.util.XmlCommandRegistrar;

import java.util.List;
import java.util.Locale;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.PrintWriter;

import jline.UnsupportedTerminal;
import jline.Terminal;

/**
 * Executes Groovy shell.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class GroovyshCommand extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Runs an interactive groovy shell";
    }

    @Override
    public int main(List<String> args, Locale locale, InputStream stdin, PrintStream stdout, PrintStream stderr) {
        // this allows the caller to manipulate the JVM state, so require the admin privilege.
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
        // TODO: ^as this class overrides main() (which has authentication stuff),
        // how to get ADMIN permission for this command?

        // this being remote means no jline capability is available
        System.setProperty("jline.terminal", UnsupportedTerminal.class.getName());
        Terminal.resetTerminal();

        Groovysh shell = createShell(stdin, stdout, stderr);
        return shell.run(args.toArray(new String[args.size()]));
    }

    protected Groovysh createShell(InputStream stdin, PrintStream stdout,
        PrintStream stderr) {

        Binding binding = new Binding();
        // redirect "println" to the CLI
        binding.setProperty("out", new PrintWriter(stdout,true));
        binding.setProperty("hudson", hudson.model.Hudson.getInstance());

        IO io = new IO(new BufferedInputStream(stdin),stdout,stderr);

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Closure registrar = new Closure(null, null) {
            public Object doCall(Object[] args) {
                assert(args.length == 1);
                assert(args[0] instanceof Shell);

                Shell shell = (Shell)args[0];
                XmlCommandRegistrar r = new XmlCommandRegistrar(shell, cl);
                r.register(GroovyshCommand.class.getResource("commands.xml"));

                return null;
            }
        };
        Groovysh shell = new Groovysh(cl, binding, io, registrar);
        shell.getImports().add("import hudson.model.*");

        // defaultErrorHook doesn't re-throw IOException, so ShellRunner in
        // Groovysh will keep looping forever if we don't terminate when the
        // channel is closed
        final Closure originalErrorHook = shell.getErrorHook();
        shell.setErrorHook(new Closure(shell, shell) {
            public Object doCall(Object[] args) throws ChannelClosedException {
                if (args.length == 1 && args[0] instanceof ChannelClosedException) {
                    throw (ChannelClosedException)args[0];
                }

                return originalErrorHook.call(args);
            }
        });

        return shell;
    }

    protected int run() {
        throw new UnsupportedOperationException();
    }
}
