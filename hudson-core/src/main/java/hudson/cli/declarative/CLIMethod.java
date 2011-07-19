/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

import hudson.cli.CLICommand;
import hudson.util.ListBoxModel.Option;
import org.jvnet.hudson.annotation_indexer.Indexed;
import org.kohsuke.args4j.Argument;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Annotates methods on model objects to expose them as CLI commands.
 *
 * <p>
 * You need to have <tt>Messages.properties</tt> in the same package with the
 * <tt>CLI.<i>command-name</i>.shortDescription</tt> key to describe the command.
 * This is used for the same purpose as {@link CLICommand#getShortDescription()}.
 *
 * <p>
 * If you put a {@link CLIMethod} on an instance method (as opposed to a static method),
 * you need a corresponding {@linkplain CLIResolver CLI resolver method}.
 *
 * <p>
 * A CLI method can have its parameters annotated with {@link Option} and {@link Argument},
 * to receive parameter/argument injections.
 *
 * <p>
 * A CLI method needs to be public.
 *
 * @author Kohsuke Kawaguchi
 * @see CLICommand
 * @since 1.321
 */
@Indexed
@Retention(RUNTIME)
@Target({METHOD})
@Documented
public @interface CLIMethod {
    /**
     * CLI command name. Used as {@link CLICommand#getName()} 
     */
    String name();
}
