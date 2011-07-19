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
import hudson.model.Hudson;
import org.jvnet.hudson.annotation_indexer.Indexed;
import org.kohsuke.args4j.CmdLineException;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Annotates a resolver method that binds a portion of the command line arguments and parameters
 * to an instance whose {@link CLIMethod} is invoked for the final processing.
 *
 * <p>
 * Hudson uses the return type of the resolver method
 * to pick the resolver method to use, of all the resolver methods it discovers. That is,
 * if Hudson is looking to find an instance of type <tt>T</tt> for the current command, it first
 * looks for the resolver method whose return type is <tt>T</tt>, then it checks for the base type of <tt>T</tt>,
 * and so on.
 *
 * <p>
 * If the chosen resolver method is an instance method on type <tt>S</tt>, the "parent resolver" is then
 * located to resolve an instance of type 'S'. This process repeats until a static resolver method is discovered
 * (since most of Hudson's model objects are anchored to the root {@link Hudson} object, normally that would become
 * the top-most resolver method.)
 *
 * <p>
 * Parameters of the resolver method receives the same parameter/argument injections that {@link CLIMethod}s receive.
 * Parameters and arguments consumed by the resolver will not be visible to {@link CLIMethod}s.
 *
 * <p>
 * The resolver method shall never return null &mdash; it should instead indicate a failure by throwing
 * {@link CmdLineException}.
 *
 * @author Kohsuke Kawaguchi
 * @see CLICommand
 * @since 1.321
 */
@Indexed
@Retention(RUNTIME)
@Target({METHOD})
@Documented
public @interface CLIResolver {
}
