/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.sonatype.matrix.smoothie.internal.extension.old;

import com.google.inject.Binder;
import hudson.Extension;
import org.sonatype.guice.asm.AnnotationVisitor;
import org.sonatype.guice.asm.ClassVisitor;
import org.sonatype.guice.asm.Opcodes;
import org.sonatype.guice.asm.Type;
import org.sonatype.guice.bean.binders.QualifiedTypeBinder;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceVisitor;
import org.sonatype.guice.bean.scanners.EmptyClassVisitor;
import org.sonatype.guice.bean.scanners.QualifiedTypeVisitor;

import javax.inject.Qualifier;
import java.net.URL;

/**
 * {@link ClassSpaceVisitor} that detects types annotated with {@link Qualifier} or {@link Extension} annotations.
 *
 * @since 1.1
 */
public final class ExtensionTypeVisitor
    extends EmptyClassVisitor
    implements ClassSpaceVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String EXTENSION_DESC = Type.getDescriptor( Extension.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final QualifiedTypeVisitor qualifiedVisitor;

    private final ExtensionTypeBinder extensionBinder;

    private ClassSpace space;

    private String clazzName;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ExtensionTypeVisitor( final Binder binder )
    {
        this.qualifiedVisitor = new QualifiedTypeVisitor( new QualifiedTypeBinder( binder ) );
        this.extensionBinder = new ExtensionTypeBinder( binder );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void visit( final ClassSpace _space )
    {
        qualifiedVisitor.visit( _space );
        space = _space;
    }

    public ClassVisitor visitClass( final URL url )
    {
        qualifiedVisitor.visitClass( url );
        clazzName = null;
        return this;
    }

    @Override
    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        qualifiedVisitor.visit( version, access, name, signature, superName, interfaces );
        if ( ( access & ( Opcodes.ACC_INTERFACE | access & Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC ) ) == 0 )
        {
            clazzName = name; // concrete type
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        if ( null != clazzName && EXTENSION_DESC.equals( desc ) )
        {
            final Class<?> clazz = space.loadClass( clazzName.replace( '/', '.' ) );
            extensionBinder.bindExtension( clazz.getAnnotation( Extension.class ), clazz );
        }
        return qualifiedVisitor.visitAnnotation( desc, visible );
    }
}
