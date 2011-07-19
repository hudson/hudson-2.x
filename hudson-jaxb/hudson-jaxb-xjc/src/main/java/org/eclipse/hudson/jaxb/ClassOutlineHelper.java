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
