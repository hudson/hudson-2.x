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

import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

import java.util.HashMap;
import java.util.Map;

/**
 * Stateful helper for {@link ClassOutline} operations.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class ClassOutlineHelper
{
    private static final String ACCESSOR_PREFIX_GET = "get";
    private static final String ACCESSOR_PREFIX_IS = "is";

    private final Map<String, JMethod> methodMap;

    /**
     * @param outline the ClassOutline to perform operations on
     */
    public ClassOutlineHelper(final ClassOutline outline) {
        methodMap = generateMethodIndex(outline);

    }

    /**
     * Finds 'get' and 'is' accessor methods for the specified field.
     * 
     * @return the accessor method matching the field or null if none
     */
    public JMethod findAccessor(final FieldOutline field) {
        // Get the public name for the field.
        String name = field.getPropertyInfo().getName(true);

        // Look for a 'get' then 'is' accessors.
        JMethod accessorMethod = methodMap.get(ACCESSOR_PREFIX_GET + name);
        if (accessorMethod == null) {
            accessorMethod = methodMap.get(ACCESSOR_PREFIX_IS + name);
        }
        return accessorMethod;
    }

    private Map<String, JMethod> generateMethodIndex(final ClassOutline outline) {
        Map<String, JMethod> map = new HashMap<String, JMethod>();
        for (JMethod classMethod : outline.implClass.methods()) {
            map.put(classMethod.name(), classMethod);
        }
        return map;
    }
}
