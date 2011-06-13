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
