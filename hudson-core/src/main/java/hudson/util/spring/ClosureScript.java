/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.util.spring;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

/**
 * {@link Script} that performs method invocations and property access like {@link Closure} does.
 *
 * <p>
 * For example, when the script is:
 *
 * <pre>
 * a = 1;
 * b(2);
 * <pre>
 *
 * <p>
 * Using {@link ClosureScript} as the base class would run it as:
 *
 * <pre>
 * delegate.a = 1;
 * delegate.b(2);
 * </pre>
 *
 * ... whereas in plain {@link Script}, this will be run as:
 *
 * <pre>
 * binding.setProperty("a",1);
 * ((Closure)binding.getProperty("b")).call(2);
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
// TODO: moved to stapler
public abstract class ClosureScript extends Script {
    private GroovyObject delegate;

    protected ClosureScript() {
        super();
    }

    protected ClosureScript(Binding binding) {
        super(binding);
    }

    /**
     * Sets the delegation target.
     */
    public void setDelegate(GroovyObject delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            return delegate.invokeMethod(name,args);
        } catch (MissingMethodException mme) {
            return super.invokeMethod(name, args);
        }
    }

    @Override
    public Object getProperty(String property) {
        try {
            return delegate.getProperty(property);
        } catch (MissingPropertyException e) {
            return super.getProperty(property);
        }
    }

    @Override
    public void setProperty(String property, Object newValue) {
        try {
            delegate.setProperty(property,newValue);
        } catch (MissingPropertyException e) {
            super.setProperty(property,newValue);
        }
    }
}
