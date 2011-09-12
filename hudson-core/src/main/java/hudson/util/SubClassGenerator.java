/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.util;

import hudson.PluginManager.UberClassLoader;
import hudson.model.Hudson;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates a new class that just defines constructors into the super types.
 *
 * @author Kohsuke Kawaguchi
 */
public class SubClassGenerator extends ClassLoader {
    public SubClassGenerator(ClassLoader parent) {
        super(parent);
    }

    public <T> Class<? extends T> generate(Class<T> base, String name) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);//?
        cw.visit(49, ACC_PUBLIC, name.replace('.', '/'), null,
                Type.getInternalName(base),null);

        for (Constructor c : base.getDeclaredConstructors()) {
            Class[] et = c.getExceptionTypes();
            String[] exceptions = new String[et.length];
            for (int i = 0; i < et.length; i++)
                exceptions[i] = Type.getInternalName(et[i]);

            String methodDescriptor = getMethodDescriptor(c);
            MethodVisitor m = cw.visitMethod(c.getModifiers(), "<init>", methodDescriptor, null, exceptions);
            m.visitCode();

            int index=1;
            m.visitVarInsn(ALOAD,0);
            for (Class param : c.getParameterTypes()) {
                Type t = Type.getType(param);
                m.visitVarInsn(t.getOpcode(ILOAD), index);
                index += t.getSize();
            }
            m.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(base), "<init>", methodDescriptor);
            m.visitInsn(RETURN);
            m.visitMaxs(index,index);
            m.visitEnd();
        }

        cw.visitEnd();
        byte[] image = cw.toByteArray();

        Class<? extends T> c = defineClass(name, image, 0, image.length).asSubclass(base);

        Hudson h = Hudson.getInstance();
        if (h!=null)    // null only during tests.
            ((UberClassLoader)h.pluginManager.uberClassLoader).addNamedClass(name,c); // can't change the field type as it breaks binary compatibility
        
        return c;
    }

    private String getMethodDescriptor(Constructor c) {
        StringBuilder buf = new StringBuilder();
        buf.append('(');
        for (Class p : c.getParameterTypes())
            buf.append(Type.getDescriptor(p));

        buf.append(")V");
        return buf.toString();
    }
}
