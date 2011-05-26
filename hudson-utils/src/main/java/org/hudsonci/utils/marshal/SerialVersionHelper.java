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

package org.hudsonci.utils.marshal;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.sql.rowset.serial.SerialArray;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class SerialVersionHelper
{
    private static final Method LOOKUP_METHOD;

    static {
        Method lookupMethod;
        try {
            lookupMethod = ObjectStreamClass.class.getMethod( "lookupAny", Class.class );
        } catch (Throwable e) {
            lookupMethod = null;
        }
        LOOKUP_METHOD = lookupMethod;
    }
    
    public static Long getFromAnnotation(final Object source) {
        SerialVersion version = source.getClass().getAnnotation(SerialVersion.class);
        if (version != null) {
            return version.value();
        }
        return null;
    }

    public static Long getFromField(final Object source) {
        Class type = source.getClass();
        try {
            Field field = type.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            return (Long) field.get(source);
        }
        catch (Exception e) {
            // ignore
        }
        if (LOOKUP_METHOD != null) {
            try {
                return ((ObjectStreamClass)LOOKUP_METHOD.invoke(null, type)).getSerialVersionUID();
            } catch (Throwable e) {}
        }
        return ObjectStreamClass.lookup(type).getSerialVersionUID();
    }

    public static long get(final Object source) {
        Long version = getFromAnnotation(source);
        if (version == null) {
            version = getFromField(source);
        }
        return version;
    }
}
