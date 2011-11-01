/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Nikita Levyankov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.hudsonci.api.model;

import hudson.model.Label;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import java.util.Map;

/**
 * Interface that reflects common methods for AbstractProject model
 * <p/>
 * Date: 9/15/11
 *
 * @author Nikita Levyankov
 */
public interface IAbstractProject extends IJob {
    /**
     * Returns configured SCM for project,
     *
     * @return {@link SCM} instance
     */
    SCM getScm();

    /**
     * Returns map of triggers.
     *
     * @return {@link Map}.
     */
    Map<TriggerDescriptor, Trigger> getTriggers();

    /**
     * Gets the specific trigger, should be null if the property is not configured for this job.
     *
     * @param clazz class of trigger
     * @return T
     */
    <T extends Trigger> T getTrigger(Class<T> clazz);

    /**
     * Checks whether workspace should be cleaned before build
     *
     * @return boolean value
     */
    boolean isCleanWorkspaceRequired();

    /**
     * Indicates whether build should be blocked while downstream project is building.
     *
     * @return true if yes, false if no, null - if value should be taken from parent
     */
    boolean blockBuildWhenDownstreamBuilding();

    /**
     * Indicates whether build should be blocked while upstream project is building.
     *
     * @return true if yes, false if no, null - if value should be taken from parent
     */
    boolean blockBuildWhenUpstreamBuilding();

    /**
     * Returns scm checkout retry count.
     *
     * @return int value.
     */
    int getScmCheckoutRetryCount();

    /**
     * Returns project quiet period.
     *
     * @return int value.
     */
    int getQuietPeriod();

    /**
     * If this project is configured to be always built on this node,
     * return that {@link hudson.model.Node}. Otherwise null.
     *
     * @return {@link hudson.model.Label} instance.
     */
    Label getAssignedLabel();

    /**
     * Gets the textual representation of the assigned label as it was entered by the user.
     *
     * @return string
     */
    String getAssignedLabelString();

    /**
     * Gets whether this project is using the advanced affinity chooser UI.
     *
     * @return true - advanced chooser, false - simple textfield.
     */
    //TODO this method is UI only. Investigate how-to remove it from model.
    boolean isAdvancedAffinityChooser();
}
