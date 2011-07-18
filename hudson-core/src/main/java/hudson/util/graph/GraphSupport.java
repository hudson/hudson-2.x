/*
 * The MIT License
 * 
 * Copyright (c) 2011, Oracle Corporation, Winston Prakash 
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
