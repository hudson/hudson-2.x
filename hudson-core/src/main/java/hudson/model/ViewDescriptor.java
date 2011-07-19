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

package hudson.model;

/**
 * {@link Descriptor} for {@link View}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.269
 */
public abstract class ViewDescriptor extends Descriptor<View> {
    /**
     * Returns the human-readable name of this type of view. Used
     * in the view creation screen. The string should look like
     * "Abc Def Ghi".
     */
    public abstract String getDisplayName();

    /**
     * Some special views are not instantiable, and for those
     * this method returns false.
     */
    public boolean isInstantiable() {
        return true;
    }

    /**
     * Jelly fragment included in the "new view" page.
     */
    public final String getNewViewDetailPage() {
        return '/'+clazz.getName().replace('.','/').replace('$','/')+"/newViewDetail.jelly";
    }

    protected ViewDescriptor(Class<? extends View> clazz) {
        super(clazz);
    }

    protected ViewDescriptor() {
    }
}
