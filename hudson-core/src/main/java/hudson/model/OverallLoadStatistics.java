/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi, Seiji Sogabe, Winston Prakash
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.util.ColorPalette;
import hudson.util.graph.MultiStageTimeSeries;
import hudson.util.graph.MultiStageTimeSeries.TimeScale;
import hudson.util.graph.MultiStageTimeSeries.TrendChart;
import java.awt.Color;
import org.kohsuke.stapler.export.Exported;

/**
 * {@link LoadStatistics} for the entire system (the master and all the slaves combined.)
 *
 * <p>
 * {@link #computeQueueLength()} and {@link #queueLength} counts those tasks
 * that are unassigned to any node, whereas {@link #totalQueueLength}
 * tracks the queue length including tasks that are assigned to a specific node.
 *
 * @author Kohsuke Kawaguchi
 * @see Hudson#overallLoad
 */
public class OverallLoadStatistics extends LoadStatistics {
    /**
     * Number of total {@link Queue.BuildableItem}s that represents blocked builds.
     */
    //TODO: review and check whether we can do it private
    @Exported
    public final MultiStageTimeSeries totalQueueLength = new MultiStageTimeSeries(
            Messages._LoadStatistics_Legends_QueueLength(), ColorPalette.GREY, 0,DECAY);

    /*package*/ OverallLoadStatistics() {
        super(0,0);
    }

    public MultiStageTimeSeries getTotalQueueLength() {
        return totalQueueLength;
    }

    @Override
    public int computeIdleExecutors() {
        return new ComputerSet().getIdleExecutors();
    }

    @Override
    public int computeTotalExecutors() {
        return new ComputerSet().getTotalExecutors();
    }

    @Override
    public int computeQueueLength() {
        return Hudson.getInstance().getQueue().countBuildableItemsFor(null);
    }

    /**
     * When drawing the overall load statistics, use the total queue length,
     * not {@link #queueLength}, which just shows jobs that are to be run on the master. 
     */
    protected TrendChart createOverallTrendChart(TimeScale timeScale) {
        return MultiStageTimeSeries.createTrendChart(timeScale,busyExecutors,totalExecutors,totalQueueLength);
    }
}
