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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.DOMUtils;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Allows arbitrary code to be inserted into generated types.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class CodeInsertPlugin
    extends AbstractParameterizablePlugin
{
    public static final String NS = "http://hudson-ci.org/jaxb/hudson/code-insert";

    public static final String CODE = "code";

    public static final String IMPORT = "import";

    @Override
    public String getOptionName() {
        return "XcodeInsert";
    }

    @Override
    public String getUsage() {
        return "Allows arbitrary code to be inserted into generated types.";
    }

    @Override
    public Collection<QName> getCustomizationElementNames() {
        return Arrays.asList(new QName(NS, CODE), new QName(NS, IMPORT));
    }

    @Override
    protected boolean run(final Outline outline, final Options options) throws Exception {
        assert outline != null;
        assert options != null;

        for (ClassOutline type : outline.getClasses()) {
            process(type.implClass, type.target.getCustomizations(), type.parent());
        }

        for (EnumOutline type : outline.getEnums()) {
            process(type.clazz, type.target.getCustomizations(), type.parent());
        }

        return true;
    }

    private void process(final JDefinedClass type, final CCustomizations customizations, final Outline parent) {
        processImport(type, customizations, parent);
        processCode(type, customizations, parent);
    }

    private void processImport(final JDefinedClass type, final CCustomizations customizations, final Outline parent) {
        assert type != null;
        assert customizations != null;
        assert parent != null;

        CPluginCustomization custom = customizations.find(NS, IMPORT);
        if (custom != null) {
            custom.markAsAcknowledged();

            String body = DOMUtils.getElementText(custom.element);
            String[] tmp = body.trim().split("\\s+");
            List<String> names = new ArrayList<String>();

            // Build a list of class names
            for (String name : tmp) {
                if (name.trim().length() == 0) {
                    continue;
                }
                names.add(name);
            }

            // Only generate the dummy block if we have some names to import
            if (!names.isEmpty()) {
                // Setup dummy static + if(false) to force XJC to import
                JBlock block = type.init()._if(JExpr.direct("false"))._then();

                // Add dummy toString() call for each imported class
                for (String name : names) {
                    JClass ref = parent.getCodeModel().ref(name);
                    // Generate dummy reference to force import
                    block.invoke(ref.dotclass(), "toString");
                }
            }
        }
    }

    private void processCode(final JDefinedClass type, final CCustomizations customizations, final Outline parent) {
        assert type != null;
        assert customizations != null;
        assert parent != null;

        CPluginCustomization custom = customizations.find(NS, CODE);
        if (custom != null) {
            custom.markAsAcknowledged();
            String body = DOMUtils.getElementText(custom.element);
            type.direct(body);
        }
    }
}
