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

package hudson.model;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.apache.commons.lang3.StringUtils;
import net.sf.json.JSONObject;
import hudson.Extension;
import hudson.cli.CLICommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;

/**
 * @author huybrechts
 */
public class ChoiceParameterDefinition extends SimpleParameterDefinition {
    private final List<String> choices;

    @DataBoundConstructor
    public ChoiceParameterDefinition(String name, String choices, String description) {
        super(name, description);
        this.choices = Arrays.asList(choices.split("\\r?\\n"));
        if (choices.length()==0) {
            throw new IllegalArgumentException("No choices found");
        }
    }

    public ChoiceParameterDefinition(String name, String[] choices, String description) {
        super(name, description);
        this.choices = new ArrayList<String>(Arrays.asList(choices));
        if (this.choices.isEmpty()) {
            throw new IllegalArgumentException("No choices found");
        }
    }
    
    @Exported
    public List<String> getChoices() {
        return choices;
    }

    public String getChoicesText() {
        return StringUtils.join(choices, "\n");
    }

    @Override
    public StringParameterValue getDefaultParameterValue() {
        return new StringParameterValue(getName(), choices.get(0), getDescription());
    }


    private StringParameterValue checkValue(StringParameterValue value) {
        if (!choices.contains(value.value))
            throw new IllegalArgumentException("Illegal choice: " + value.value);
        return value;
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        StringParameterValue value = req.bindJSON(StringParameterValue.class, jo);
        value.setDescription(getDescription());
        return checkValue(value);
    }

    public StringParameterValue createValue(String value) {
        return checkValue(new StringParameterValue(getName(), value, getDescription()));
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.ChoiceParameterDefinition_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/help/parameter/choice.html";
        }
    }

}
