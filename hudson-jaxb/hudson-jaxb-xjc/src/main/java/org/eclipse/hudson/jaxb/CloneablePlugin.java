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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;

/**
 * Adds {@link Cloneable} and {@link Object#clone} implementation to all classes.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class CloneablePlugin
    extends AbstractParameterizablePlugin
{
    @Override
    public String getOptionName() {
        return "Xcloneable";
    }

    @Override
    public String getUsage() {
        return "Adds Cloneable and Object.clone() implementation to all classes.";
    }

    @Override
    protected boolean run(final Outline outline, final Options options) throws Exception {
        assert outline != null;
        assert options != null;

        for (ClassOutline type : outline.getClasses()) {
            processClassOutline(type);
        }

        return true;
    }

    private void processClassOutline(final ClassOutline outline) {
        assert outline != null;

        JDefinedClass type = outline.implClass;
        JCodeModel model = type.owner();
        type._implements(model.ref(Cloneable.class));

        JMethod method = type.method(JMod.PUBLIC, outline.implRef, "clone");
        method.annotate(Override.class);

        JBlock body = method.body();
        JTryBlock block = body._try();
        block.body()._return(JExpr.cast(outline.implRef, JExpr._super().invoke("clone")));
        block._catch(model.ref(CloneNotSupportedException.class)).body()._throw(JExpr._new(model.ref(InternalError.class)));
    }
}
