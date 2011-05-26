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
