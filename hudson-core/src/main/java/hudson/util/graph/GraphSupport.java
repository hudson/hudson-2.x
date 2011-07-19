/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Winston Prakash
 *      
 *
 *******************************************************************************/ 

package hudson.util.graph;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Extension point for adding Graph Support to Hudson
 *
 * <p>
 * This object can have an optional <tt>config.jelly</tt> to configure the Graph Support
 * <p>
 * A default constructor is needed to create GraphSupport in the default configuration.
 *
 * @author Winston Prakash
 * @since 2.0.1
 * @see GraphSupportDescriptor
 */
public abstract class GraphSupport extends AbstractDescribableImpl<GraphSupport> implements ExtensionPoint {

    /**
     * Returns all the registered {@link GraphSupport} descriptors.
     */
    public static DescriptorExtensionList<GraphSupport, Descriptor<GraphSupport>> all() {
        return Hudson.getInstance().<GraphSupport, Descriptor<GraphSupport>>getDescriptorList(GraphSupport.class);
    }

    abstract public void setChartType(int chartType);
    abstract public void setTitle(String title);
    abstract public void setXAxisLabel(String xLabel);
    abstract public void setYAxisLabel(String yLabel);
    abstract public void setData(DataSet data);
    abstract public void setMultiStageTimeSeries(List<MultiStageTimeSeries> multiStageTimeSeries);
    abstract public BufferedImage render(int width, int height);
    abstract public String getImageMap(String id, int width, int height);


    @Override
    public GraphSupportDescriptor getDescriptor() {
        return (GraphSupportDescriptor) super.getDescriptor();
    }
}
