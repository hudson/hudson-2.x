/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.jaxb;

import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;

/**
 * Replaces any <tt>isXXX()</tt> method with <tt>getXXX()</tt> where the return type is {@link Boolean}.
 * Methods with primitive <tt>boolean</tt> are left as-is.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class BooleanGetterPlugin
    extends AbstractParameterizablePlugin
{
    @Override
    public String getOptionName() {
        return "XbooleanGetter";
    }

    @Override
    public String getUsage() {
        return "Replaces isXXX() methods with getXXX() for getters of type java.lang.Boolean.";
    }

    @Override
    protected boolean run(final Outline outline, final Options options) throws Exception {
        assert outline != null;
        assert options != null;

        for (ClassOutline type : outline.getClasses()) {
            for (JMethod method : type.implClass.methods()) {
                if (method.name().startsWith("is") && method.listParams().length == 0) {
                    JType rtype = method.type();
                    if (rtype.fullName().equals(Boolean.class.getName())) {
                        method.name("get" + method.name().substring(2));
                    }
                }
            }
        }

        return true;
    }
}
