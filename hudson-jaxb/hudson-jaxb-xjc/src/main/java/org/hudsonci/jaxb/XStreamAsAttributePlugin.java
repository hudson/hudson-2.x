/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.jaxb;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.jvnet.jaxb2_commons.util.FieldAccessorUtils;

/**
 * Adds {@link XStreamAsAttribute} to XSD attribute fields.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class XStreamAsAttributePlugin
    extends AbstractParameterizablePlugin
{
    @Override
    public String getOptionName() {
        return "XStreamAsAttribute";
    }

    @Override
    public String getUsage() {
        return "Adds @XStreamAsAttribute to XSD attribute fields.";
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

        PropertyKind kind = outline.getPropertyInfo().kind();
        if (kind == PropertyKind.ATTRIBUTE) {
            JFieldVar field = FieldAccessorUtils.field(outline);
            JAnnotationUse anno = field.annotate(XStreamAsAttribute.class);
        }
    }
}
