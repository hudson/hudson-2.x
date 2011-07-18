/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson;

import org.kohsuke.stapler.QueryParameter;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Used in conjunction with {@link QueryParameter} to refer to
 * nearby parameters that belong to different parents.
 *
 * <p>
 * Currently, "..", "../..", etc. are supported to indicate
 * parameters that belong to the ancestors.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.376
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface RelativePath {
    String value();
}
