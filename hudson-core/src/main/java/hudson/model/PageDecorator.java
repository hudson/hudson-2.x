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
*    Kohsuke Kawaguchi, Erik Ramfelt
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.ExtensionPoint;
import hudson.Plugin;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.DescriptorList;

import java.util.List;

/**
 * Participates in the rendering of HTML pages for all pages of Hudson.
 *
 * <p>
 * This class provides a few hooks to augument the HTML generation process of Hudson, across
 * all the HTML pages that Hudson delivers.
 *
 * <p>
 * For example, if you'd like to add a Google Analytics stat to Hudson, then you need to inject
 * a small script fragment to all Hudson pages. This extension point provides a means to do that.
 *
 * <h2>Life-cycle</h2>
 * <p>
 * {@link Plugin}s that contribute this extension point
 * should implement a new decorator and put {@link Extension} on the class.
 *
 * <h2>Associated Views</h2>
 * <h4>global.jelly</h4>
 * <p>
 * If this extension point needs to expose a global configuration, write this jelly page.
 * See {@link Descriptor} for more about this. Optional.
 *
 * <h4>footer.jelly</h4>
 * <p>
 * This page is added right before the &lt;/body> tag. Convenient place for adding tracking beacons, etc.
 *
 * <h4>header.jelly</h4>
 * <p>
 * This page is added right before the &lt;/head> tag. Convenient place for additional stylesheet,
 * &lt;meta> tags, etc.
 *
 *
 * @author Kohsuke Kawaguchi
 * @since 1.235
 */
public abstract class PageDecorator extends Descriptor<PageDecorator> implements ExtensionPoint, Describable<PageDecorator> {
    /**
     * @param yourClass
     *      pass-in "this.getClass()" (except that the constructor parameters cannot use 'this',
     *      so you'd have to hard-code the class name.
     */
    protected PageDecorator(Class<? extends PageDecorator> yourClass) {
        super(yourClass);
    }

// this will never work because Descriptor and Describable are the same thing.
//    protected PageDecorator() {
//    }

    public final Descriptor<PageDecorator> getDescriptor() {
        return this;
    }

    /**
     * Unless this object has additional web presence, display name is not used at all.
     * So default to "".
     */
    public String getDisplayName() {
        return "";
    }

    /**
     * Obtains the URL of this object, excluding the context path.
     *
     * <p>
     * Every {@link PageDecorator} is bound to URL via {@link Hudson#getDescriptor()}.
     * This method returns such an URL.
     */
    public final String getUrl() {
        return "descriptor/"+clazz.getName();
    }

    /**
     * All the registered instances.
     * @deprecated as of 1.286
     *      Use {@link #all()} for read access, and use {@link Extension} for registration.
     */
    public static final List<PageDecorator> ALL = (List)new DescriptorList<PageDecorator>(PageDecorator.class);

    /**
     * Returns all the registered {@link PageDecorator} descriptors.
     */
    public static ExtensionList<PageDecorator> all() {
        return Hudson.getInstance().<PageDecorator,PageDecorator>getDescriptorList(PageDecorator.class);
    }
}
