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

package org.jvnet.hudson.test;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.slaves.ComputerConnector;
import hudson.slaves.ComputerConnectorDescriptor;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Test bed to verify the configuration roundtripness of the {@link ComputerConnector}.
 *
 * @author Kohsuke Kawaguchi
 * @see HudsonTestCase#computerConnectorTester
 */
public class ComputerConnectorTester extends AbstractDescribableImpl<ComputerConnectorTester> {
    public final HudsonTestCase testCase;
    public ComputerConnector connector;

    public ComputerConnectorTester(HudsonTestCase testCase) {
        this.testCase = testCase;
    }

    public void doConfigSubmit(StaplerRequest req) throws IOException, ServletException {
        connector = req.bindJSON(ComputerConnector.class, req.getSubmittedForm().getJSONObject("connector"));
    }

    public List getConnectorDescriptors() {
        return ComputerConnectorDescriptor.all();
    }
    
    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerConnectorTester> {
        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
