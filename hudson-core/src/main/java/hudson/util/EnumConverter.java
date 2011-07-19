/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import org.apache.commons.beanutils.Converter;

/**
 * {@link Converter} for enums. Used for form binding.
 * @author Kohsuke Kawaguchi
 */
public class EnumConverter implements Converter {
    public Object convert(Class aClass, Object object) {
        return Enum.valueOf(aClass,object.toString());
    }
}
