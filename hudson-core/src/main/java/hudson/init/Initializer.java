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

package hudson.init;

import org.jvnet.hudson.annotation_indexer.Indexed;
import org.jvnet.hudson.reactor.Task;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import static hudson.init.InitMilestone.STARTED;
import static hudson.init.InitMilestone.COMPLETED;

/**
 * Placed on static methods to indicate that this method is to be run during the Hudson start up to perform
 * some sort of initialization tasks.
 *
 * <h3>Example</h3>
 * <pre>
   &#64;Initializer(after=JOB_LOADED)
   public static void init() throws IOException {
       ....
   }
 * </pre>
 * 
 * @author Kohsuke Kawaguchi
 */
@Indexed
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Initializer {
    /**
     * Indicates that the specified milestone is necessary before executing this initializer.
     *
     * <p>
     * This has the identical purpose as {@link #requires()}, but it's separated to allow better type-safety
     * when using {@link InitMilestone} as a requirement (since enum member definitions need to be constant.)
     */
    InitMilestone after() default STARTED;

    /**
     * Indicates that this initializer is a necessary step before achieving the specified milestone.
     *
     * <p>
     * This has the identical purpose as {@link #attains()}. See {@link #after()} for why there are two things
     * to achieve the same goal.
     */
    InitMilestone before() default COMPLETED;

    /**
     * Indicates the milestones necessary before executing this initializer.
     */
    String[] requires() default {};

    /**
     * Indicates the milestones that this initializer contributes to.
     *
     * A milestone is considered attained if all the initializers that attains the given milestone
     * completes. So it works as a kind of join.
     */
    String[] attains() default {};

    /**
     * Key in <tt>Messages.properties</tt> that represents what this task is about. Used for rendering the progress.
     * Defaults to "${short class name}.${method Name}".
     */
    String displayName() default "";

    /**
     * Should the failure in this task prevent Hudson from starting up?
     *
     * @see Task#failureIsFatal() 
     */
    boolean fatal() default true;
}
