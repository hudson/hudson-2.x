/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

package hudson;

import net.java.sezpoz.Indexable;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a field, a method, or a class for automatic discovery, so that Hudson can locate
 * implementations of {@link ExtensionPoint}s automatically.
 *
 * <p>
 * (In contrast, in earlier Hudson, the registration was manual.)
 *
 * <p>
 * In a simplest case, put this on your class, and Hudson will create an instance of it
 * and register it to the appropriate {@link ExtensionList}.
 *
 * <p>
 * If you'd like Hudson to call
 * a factory method instead of a constructor, put this annotation on your static factory
 * method. Hudson will invoke it and if the method returns a non-null instance, it'll be
 * registered. The return type of the method is used to determine which {@link ExtensionList}
 * will get the instance.
 *
 * Finally, you can put this annotation on a static field if the field contains a reference
 * to an instance that you'd like to register.
 *
 * <p>
 * This is the default way of having your implementations auto-registered to Hudson,
 * but Hudson also supports arbitrary DI containers for hosting your implementations.
 * See {@link ExtensionFinder} for more details.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.286
 * @see <a href="http://java.net/projects/sezpoz/">SezPoz</a>
 * @see ExtensionFinder
 * @see ExtensionList
 */
@Indexable
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD})
@Documented
public @interface Extension {
    /**
     * Used for sorting extensions.
     *
     * Extensions will be sorted in the descending order of the ordinal.
     * This is a rather poor approach to the problem, so its use is generally discouraged.
     *
     * @since 1.306
     */
    double ordinal() default 0;

    /**
     * If an extension is optional, don't log any class loading errors when reading it.
     * @since 1.358
     */
    boolean optional() default false;
}
