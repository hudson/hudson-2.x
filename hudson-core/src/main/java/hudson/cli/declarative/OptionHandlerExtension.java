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

package hudson.cli.declarative;

import org.jvnet.hudson.annotation_indexer.Indexed;
import org.kohsuke.args4j.spi.OptionHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link OptionHandler}s that should be auto-discovered.
 *
 * @author Kohsuke Kawaguchi
 */
@Indexed
@Retention(RUNTIME)
@Target({TYPE})
@Documented
public @interface OptionHandlerExtension {
}
