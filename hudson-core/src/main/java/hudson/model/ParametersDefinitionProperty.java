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
 *    Kohsuke Kawaguchi,   Jean-Baptiste Quenot, Seiji Sogabe, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.AbstractList;

import javax.servlet.ServletException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.Extension;

/**
 * Keeps a list of the parameters defined for a project.
 *
 * <p>
 * This class also implements {@link Action} so that <tt>index.jelly</tt> provides
 * a form to enter build parameters. 
 */
@ExportedBean(defaultVisibility=2)
public class ParametersDefinitionProperty extends JobProperty<AbstractProject<?, ?>>
        implements Action {

    private final List<ParameterDefinition> parameterDefinitions;

    public ParametersDefinitionProperty(List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    public ParametersDefinitionProperty(ParameterDefinition... parameterDefinitions) {
        this.parameterDefinitions = Arrays.asList(parameterDefinitions);
    }
    
    public AbstractProject<?,?> getOwner() {
        return owner;
    }

    @Exported
    public List<ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    /**
     * Gets the names of all the parameter definitions.
     */
    public List<String> getParameterDefinitionNames() {
        return new AbstractList<String>() {
            public String get(int index) {
                return parameterDefinitions.get(index).getName();
            }

            public int size() {
                return parameterDefinitions.size();
            }
        };
    }

    @Override
    public Collection<Action> getJobActions(AbstractProject<?, ?> job) {
        return Collections.<Action>singleton(this);
    }

    public AbstractProject<?, ?> getProject() {
        return (AbstractProject<?, ?>) owner;
    }

    /**
     * Interprets the form submission and schedules a build for a parameterized job.
     *
     * <p>
     * This method is supposed to be invoked from {@link AbstractProject#doBuild(StaplerRequest, StaplerResponse)}.
     */
    public void _doBuild(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        if(!req.getMethod().equals("POST")) {
            // show the parameter entry form.
            req.getView(this,"index.jelly").forward(req,rsp);
            return;
        }

        List<ParameterValue> values = new ArrayList<ParameterValue>();
        
        JSONObject formData = req.getSubmittedForm();
        JSONArray a = JSONArray.fromObject(formData.get("parameter"));

        for (Object o : a) {
            JSONObject jo = (JSONObject) o;
            String name = jo.getString("name");

            ParameterDefinition d = getParameterDefinition(name);
            if(d==null)
                throw new IllegalArgumentException("No such parameter definition: " + name);
            ParameterValue parameterValue = d.createValue(req, jo);
            values.add(parameterValue);
        }

    	Hudson.getInstance().getQueue().schedule(
                owner, owner.getDelay(req), new ParametersAction(values), new CauseAction(new Cause.UserCause()));

        // send the user back to the job top page.
        rsp.sendRedirect(".");
    }

    public void buildWithParameters(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        List<ParameterValue> values = new ArrayList<ParameterValue>();
        for (ParameterDefinition d: parameterDefinitions) {
        	ParameterValue value = d.createValue(req);
        	if (value != null) {
        		values.add(value);
        	} else {
        		throw new IllegalArgumentException("Parameter " + d.getName() + " was missing.");
        	}
        }

    	Hudson.getInstance().getQueue().schedule(
                owner, owner.getDelay(req), new ParametersAction(values), owner.getBuildCause(req));

        // send the user back to the job top page.
        rsp.sendRedirect(".");
    }

    /**
     * Gets the {@link ParameterDefinition} of the given name, if any.
     */
    public ParameterDefinition getParameterDefinition(String name) {
        for (ParameterDefinition pd : parameterDefinitions)
            if (pd.getName().equals(name))
                return pd;
        return null;
    }

    @Extension
    public static class DescriptorImpl extends JobPropertyDescriptor {
        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req,
                                          JSONObject formData) throws FormException {
            if (formData.isNullObject()) {
                return null;
            }

            JSONObject parameterized = formData.getJSONObject("parameterized");

            if (parameterized.isNullObject()) {
            	return null;
            }
            
            List<ParameterDefinition> parameterDefinitions = Descriptor.newInstancesFromHeteroList(
                    req, parameterized, "parameter", ParameterDefinition.all());
            if(parameterDefinitions.isEmpty())
                return null;

            return new ParametersDefinitionProperty(parameterDefinitions);
        }

        @Override
        public String getDisplayName() {
            return Messages.ParametersDefinitionProperty_DisplayName();
        }
    }

    public String getDisplayName() {
        return null;
    }

    public String getIconFileName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }
}
