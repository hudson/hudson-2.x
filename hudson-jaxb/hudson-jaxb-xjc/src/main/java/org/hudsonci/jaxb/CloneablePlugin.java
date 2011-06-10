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
