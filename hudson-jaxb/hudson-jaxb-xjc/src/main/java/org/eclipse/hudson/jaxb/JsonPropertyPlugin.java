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
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.jvnet.jaxb2_commons.util.FieldAccessorUtils;

/**
 * Adds {@link JsonProperty} to fields.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class JsonPropertyPlugin
    extends AbstractParameterizablePlugin
{
    @Override
    public String getOptionName() {
        return "XjsonProperty";
    }

    @Override
    public String getUsage() {
        return "Adds @JsonProperty to fields.";
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

        for (FieldOutline field : outline.getDeclaredFields()) {
            processFieldOutline(field);
        }
    }

    private void processFieldOutline(final FieldOutline outline) {
        assert outline != null;

        JFieldVar field = FieldAccessorUtils.field(outline);
        JAnnotationUse anno = field.annotate(JsonProperty.class);
        anno.param("value", field.name());
    }
}
