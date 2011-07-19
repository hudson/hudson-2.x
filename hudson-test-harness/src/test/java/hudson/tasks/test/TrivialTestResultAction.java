/*******************************************************************************
 *
 * Copyright (c) 2009, Yahoo!, Inc.
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

package hudson.tasks.test;

import hudson.model.AbstractBuild;
import org.kohsuke.stapler.StaplerProxy;

/**
 * A class to exercise the TestResult extension mechanism.
 */
public class TrivialTestResultAction extends AbstractTestResultAction<TrivialTestResultAction> implements StaplerProxy {

    protected TrivialTestResult result;
    protected TrivialTestResultAction(AbstractBuild owner, TrivialTestResult result) {
        super(owner);
        this.result = result;
        this.result.setParentAction(this);
    }

    /**
     * Gets the number of failed tests.
     */
    @Override
    public int getFailCount() {
        return 0;  // (FIXME: generated)
    }

    /**
     * Gets the total number of tests.
     */
    @Override
    public int getTotalCount() {
        return 0;  // (FIXME: generated)
    }

    /**
     * Returns the object that represents the actual test result.
     * This method is used by the remote API so that the XML/JSON
     * that we are sending won't contain unnecessary indirection
     * (that is, {@link AbstractTestResultAction} in between.
     * <p/>
     * <p/>
     * If such a concept doesn't make sense for a particular subtype,
     * return <tt>this</tt>.
     */
    @Override
    public Object getResult() {
        return result;
    }

    /**
     * Returns the object that is responsible for processing web requests.
     *
     * @return If null is returned, it generates 404.
     *         If {@code this} object is returned, no further
     *         {@link org.kohsuke.stapler.StaplerProxy} look-up is done and {@code this} object
     *         processes the request.
     */
    public Object getTarget() {
        return getResult();
    }
}
