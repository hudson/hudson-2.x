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

package hudson.search;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Indicates a {@link SearchItem} field/getter.
 *
 * <p>
 * Fields/getters annotated with this annotation must:
 * <ul>
 * <li>be on the class that extends from {@link SearchableModelObject}.
 * <li>have the return type / field type of {@link SearchableModelObject} (or its subtype.)
 * </ul>
 *
 * <p>
 * Such getter/field indicates an edge in the search graph, and will be added
 * automatically by {@link SearchIndexBuilder#addAllAnnotations(SearchableModelObject)}
 * to a search index. 
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface QuickSilver {
    String[] value() default {};
}
