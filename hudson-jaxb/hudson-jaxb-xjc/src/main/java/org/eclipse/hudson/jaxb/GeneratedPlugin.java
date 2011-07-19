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
import com.sun.tools.xjc.Driver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;

import javax.annotation.Generated;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Adds {@link Generated} to generated types.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class GeneratedPlugin
    extends AbstractParameterizablePlugin
{
    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public String getOptionName() {
        return "Xgenerated";
    }

    @Override
    public String getUsage() {
        return "Adds @Generated to generated types.";
    }

    @Override
    protected boolean run(final Outline outline, final Options options) throws Exception {
        assert outline != null;
        assert options != null;

        for (ClassOutline type : outline.getClasses()) {
            addGenerated(type.implClass);
        }

        for (EnumOutline type : outline.getEnums()) {
            addGenerated(type.clazz);
        }

        return true;
    }

    private void addGenerated(final JDefinedClass type) {
        assert type != null;

        JAnnotationUse anno = type.annotate(Generated.class);
        anno.param("value", String.format("XJC %s", Driver.getBuildID()));

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_FORMAT);
        anno.param("date", sdf.format(now));

        // TODO: Maybe support customized comments?
    }
}
