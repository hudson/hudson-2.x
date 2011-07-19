/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.util;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Used to build up launch parameters for a Java virtual machine.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.361
 */
public class JVMBuilder implements Serializable {
    private final ClasspathBuilder classpath = new ClasspathBuilder();
    private final Map<String,String> systemProperties = new TreeMap<String,String>();
    private final ArgumentListBuilder args = new ArgumentListBuilder();
    private final ArgumentListBuilder vmopts = new ArgumentListBuilder();
    private FilePath pwd;

    private String mainClass;

    /**
     * Returns a builder object for creating classpath arguments.
     */
    public ClasspathBuilder classpath() {
        return classpath;
    }

    public JVMBuilder systemProperty(String key, String value) {
        this.systemProperties.put(key,value);
        return this;
    }

    public Map<String,String> systemProperties() {
        return this.systemProperties;
    }

    public JVMBuilder systemProperties(Map<String,String> props) {
        if (props!=null)    this.systemProperties.putAll(props);
        return this;
    }

    /**
     * Arguments to the main class.
     */
    public ArgumentListBuilder args() {
        return args;
    }

    /**
     * JVM options.
     */
    public ArgumentListBuilder vmopts() {
        return vmopts;
    }

    /**
     * Sets the current directory for the new JVM.
     */
    public JVMBuilder pwd(FilePath pwd) {
        this.pwd = pwd;
        return this;
    }

    /**
     * Enables the debugger support on the given port.
     */
    public JVMBuilder debug(int port) {
        vmopts.add("-Xrunjdwp:transport=dt_socket,server=y,address="+port);
        return this;
    }

    /**
     * Sets the current directory for the new JVM.
     * This overloaded version only makes sense when you are launching JVM locally.
     */
    public JVMBuilder pwd(File pwd) {
        return pwd(new FilePath(pwd));
    }

    public JVMBuilder mainClass(String fullyQualifiedClassName) {
        this.mainClass = fullyQualifiedClassName;
        return this;
    }

    public JVMBuilder mainClass(Class mainClass) {
        return mainClass(mainClass.getName());
    }

    public ArgumentListBuilder toFullArguments() {
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(new File(System.getProperty("java.home"),"bin/java")); // TODO: if we are to support a remote launch, JVM would be on a different path.
        args.addKeyValuePairs("-D",systemProperties);
        args.add("-cp").add(classpath.toString());
        args.add(this.vmopts.toCommandArray());
        args.add(mainClass);
        args.add(this.args.toCommandArray());
        return args;
    }

    /**
     * Fills a {@link ProcStarter} with all the parameters configured by this builder.
     */
    public ProcStarter launch(Launcher launcher) {
        return launcher.launch().cmds(toFullArguments()).pwd(pwd);
    }


    private static final long serialVersionUID = 1L;
}
