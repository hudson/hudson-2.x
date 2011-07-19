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
*    Kohsuke Kawaguchi, Jean-Baptiste Quenot, Martin Eigenbrodt
 *     
 *
 *******************************************************************************/ 

package hudson.triggers;

import static hudson.Util.fixNull;
import hudson.model.BuildableItem;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.scheduler.CronTabList;
import hudson.util.FormValidation;
import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import antlr.ANTLRException;

/**
 * {@link Trigger} that runs a job periodically.
 *
 * @author Kohsuke Kawaguchi
 */
public class TimerTrigger extends Trigger<BuildableItem> {

    @DataBoundConstructor
    public TimerTrigger(String spec) throws ANTLRException {
        super(spec);
    }

    @Override
    public void run() {
        job.scheduleBuild(0, new TimerTriggerCause());
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {
        public boolean isApplicable(Item item) {
            return item instanceof BuildableItem;
        }

        public String getDisplayName() {
            return Messages.TimerTrigger_DisplayName();
        }

        // backward compatibility
        public FormValidation doCheck(@QueryParameter String value) {
            return doCheckSpec(value);
        }
        
        /**
         * Performs syntax check.
         */
        public FormValidation doCheckSpec(@QueryParameter String value) {
            try {
                String msg = CronTabList.create(fixNull(value)).checkSanity();
                if(msg!=null)   return FormValidation.warning(msg);
                return FormValidation.ok();
            } catch (ANTLRException e) {
                return FormValidation.error(e.getMessage());
            }
        }
    }
    
    public static class TimerTriggerCause extends Cause {
        @Override
        public String getShortDescription() {
            return Messages.TimerTrigger_TimerTriggerCause_ShortDescription();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof TimerTriggerCause;
        }

        @Override
        public int hashCode() {
            return 5;
        }
    }
}
