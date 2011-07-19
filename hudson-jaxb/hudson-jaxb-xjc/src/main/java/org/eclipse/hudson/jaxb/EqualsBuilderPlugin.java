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
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 * Adds {@link Object#equals} methods to all classes.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class EqualsBuilderPlugin
    extends AbstractIdentityBuilderPlugin
{
    public static final String GWT = "com.flipthebird.gwthashcodeequals.EqualsBuilder";
    
    public static final String COMMONS = "org.apache.commons.lang.builder.EqualsBuilder";
    
    private String builderType = GWT;
    
    public String getBuilderType() {
        return builderType;
    }
    
    public void setBuilderType(final String builderType) {
        assert builderType != null;
        this.builderType = builderType;
    }
    
    @Override
    public String getOptionName() {
        return "XequalsBuilder";
    }

    @Override
    public String getUsage() {
        return "Adds equals() to all classes.";
    }
    
    protected void processClassOutline(final ClassOutline outline) {
        assert outline != null;

        JDefinedClass type = outline.implClass;
        JCodeModel model = type.owner();

        JMethod method = type.method(JMod.PUBLIC, model.BOOLEAN, "equals");
        method.annotate(Override.class);

        JVar param = method.param(JMod.FINAL, Object.class, "obj");
        JBlock body = method.body();

        body._if(JOp.eq(param, JExpr._null()))._then()._return(JExpr.FALSE);
        body._if(JExpr._this().eq(param))._then()._return(JExpr.TRUE);
        body._if(JOp.not(param._instanceof(type)))._then()._return(JExpr.FALSE);

        JVar that = body.decl(JMod.FINAL, type, "that", JExpr.cast(type, param));
        JType builderType = model.ref(getBuilderType());
        JVar builder = body.decl(JMod.FINAL, builderType, "builder", JExpr._new(builderType));

        // Use accessors because collection based fields are lazy initialized to
        // empty collections on access and won't always be equal via field access.
        // I.e. null vs empty collections.
        ClassOutlineHelper helper = new ClassOutlineHelper(outline);
        for (FieldOutline field : outline.getDeclaredFields()) {
            if (isFieldApplicable(field)) {
                JMethod accessor = helper.findAccessor(field);
                if (accessor != null) {
                    body.add(builder.invoke("append").arg(JExpr._this().invoke(accessor)).arg(that.invoke(accessor)));
                }
            }
        }

        body._return(builder.invoke("build"));
    }
}
