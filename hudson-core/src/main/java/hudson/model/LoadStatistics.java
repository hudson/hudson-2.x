/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Seiji Sogabe, Winston Prakash
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
package hudson.model;

import hudson.Extension;
import hudson.util.ColorPalette;
import hudson.util.graph.MultiStageTimeSeries;
import hudson.util.graph.MultiStageTimeSeries.TimeScale;
import hudson.util.graph.MultiStageTimeSeries.TrendChart;
import java.awt.Color;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.List;

/**
 * Utilization statistics for a node or a set of nodes.
 *
 * <h2>Implementation Note</h2>
 * <p>
 * Instances of this class is not capable of updating the statistics itself
 * &mdash; instead, it's done by the {@link LoadStatisticsUpdater} timer.
 * This is more efficient (as it allows us a single pass to update all stats),
 * but it's not clear to me if the loss of autonomy is worth it.
 *
 * @author Kohsuke Kawaguchi
 * @see Label#loadStatistics
 * @see Hudson#overallLoad
 */
@ExportedBean
public abstract class LoadStatistics {
    /**
     * Number of busy executors and how it changes over time.
     */
    @Exported
    public final MultiStageTimeSeries busyExecutors;

    /**
     * Number of total executors and how it changes over time.
     */
    @Exported
    public final MultiStageTimeSeries totalExecutors;

    /**
     * Number of {@link Queue.BuildableItem}s that can run on any node in this node set but blocked.
     */
    @Exported
    public final MultiStageTimeSeries queueLength;

    protected LoadStatistics(int initialTotalExecutors, int initialBusyExecutors) {
        this.totalExecutors = new MultiStageTimeSeries(
                Messages._LoadStatistics_Legends_TotalExecutors(), ColorPalette.BLUE, initialTotalExecutors,DECAY);
        this.busyExecutors = new MultiStageTimeSeries(
                Messages._LoadStatistics_Legends_BusyExecutors(), ColorPalette.RED, initialBusyExecutors,DECAY);
        this.queueLength = new MultiStageTimeSeries(
                Messages._LoadStatistics_Legends_QueueLength(),ColorPalette.GREY, 0, DECAY);
    }

    public float getLatestIdleExecutors(TimeScale timeScale) {
        return totalExecutors.pick(timeScale).getLatest() - busyExecutors.pick(timeScale).getLatest();
    }

    /**
     * Computes the # of idle executors right now and obtains the snapshot value.
     */
    public abstract int computeIdleExecutors();

    /**
     * Computes the # of total executors right now and obtains the snapshot value.
     */
    public abstract int computeTotalExecutors();

    /**
     * Computes the # of queue length right now and obtains the snapshot value.
     */
    public abstract int computeQueueLength();

    /**
     * Creates {@link CategoryDataset} which then becomes the basis
     * of the load statistics graph.
     */
    public TrendChart createTrendChart(TimeScale timeScale) {
        return MultiStageTimeSeries.createTrendChart(timeScale, totalExecutors, busyExecutors, queueLength);
    }

    /**
     * Generates the load statistics graph.
     */
    public TrendChart doGraph(@QueryParameter String type) throws IOException {
        return createTrendChart(TimeScale.parse(type));
    }

    public Api getApi() {
        return new Api(this);
    }

    /**
     * With 0.90 decay ratio for every 10sec, half reduction is about 1 min.
     */
    public static final float DECAY = Float.parseFloat(System.getProperty(LoadStatistics.class.getName()+".decay","0.9"));
    /**
     * Load statistics clock cycle in milliseconds. Specify a small value for quickly debugging this feature and node provisioning through cloud.
     */
    public static int CLOCK = Integer.getInteger(LoadStatistics.class.getName()+".clock",10*1000);

    /**
     * Periodically update the load statistics average.
     */
    @Extension
    public static class LoadStatisticsUpdater extends PeriodicWork {
        public long getRecurrencePeriod() {
            return CLOCK;
        }

        protected void doRun() {
            Hudson h = Hudson.getInstance();
            List<hudson.model.Queue.BuildableItem> bis = h.getQueue().getBuildableItems();

            // update statistics on slaves
            for( Label l : h.getLabels() ) {
                l.loadStatistics.totalExecutors.update(l.getTotalExecutors());
                l.loadStatistics.busyExecutors .update(l.getBusyExecutors());

                int q=0;
                for (hudson.model.Queue.BuildableItem bi : bis) {
                    if(bi.task.getAssignedLabel()==l)
                        q++;
                }
                l.loadStatistics.queueLength.update(q);
            }

            // update statistics of the entire system
            ComputerSet cs = new ComputerSet();
            h.overallLoad.totalExecutors.update(cs.getTotalExecutors());
            h.overallLoad.busyExecutors .update(cs.getBusyExecutors());
            int q=0;
            for (hudson.model.Queue.BuildableItem bi : bis) {
                if(bi.task.getAssignedLabel()==null)
                    q++;
            }
            h.overallLoad.queueLength.update(q);
            h.overallLoad.totalQueueLength.update(bis.size());
        }
    }
}
