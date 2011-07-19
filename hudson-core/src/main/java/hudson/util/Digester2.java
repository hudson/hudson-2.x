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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * {@link Digester} wrapper to fix the issue DIGESTER-118.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.125
 */
public class Digester2 extends Digester {
    @Override
    public void addObjectCreate(String pattern, Class clazz) {
        addRule(pattern,new ObjectCreateRule2(clazz));
    }

    private static final class ObjectCreateRule2 extends Rule {
        private final Class clazz;
        
        public ObjectCreateRule2(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public void begin(String namespace, String name, Attributes attributes) throws Exception {
            Object instance = clazz.newInstance();
            digester.push(instance);
        }

        @Override
        public void end(String namespace, String name) throws Exception {
            digester.pop();
        }
    }
}
