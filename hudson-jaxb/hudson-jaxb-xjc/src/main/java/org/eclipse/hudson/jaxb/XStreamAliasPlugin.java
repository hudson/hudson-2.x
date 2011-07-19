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

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;

import javax.xml.namespace.QName;

/**
 * Adds {@link XStreamAlias} to generated types.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class XStreamAliasPlugin
    extends AbstractParameterizablePlugin
{
    @Override
    public String getOptionName() {
        return "XxstreamAlias";
    }

    @Override
    public String getUsage() {
        return "Adds @XStreamAlias to generated types.";
    }

    @Override
    protected boolean run(final Outline outline, final Options options) throws Exception {
        assert outline != null;
        assert options != null;

        for (ClassOutline type : outline.getClasses()) {
            QName qname = type.target.getTypeName();
            if (qname != null) {
                addAlias(type.implClass, qname);
            }
        }

        for (EnumOutline type : outline.getEnums()) {
            QName qname = type.target.getTypeName();
            if (qname != null) {
                addAlias(type.clazz, qname);
            }
        }

        return true;
    }

    private void addAlias(final JDefinedClass type, final QName qname) {
        assert type != null;
        assert qname != null;
        JAnnotationUse anno = type.annotate(XStreamAlias.class);
        anno.param("value", qname.getLocalPart());
    }
}
