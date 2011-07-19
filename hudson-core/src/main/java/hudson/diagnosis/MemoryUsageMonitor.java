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
 *    Kohsuke Kawaguchi, Winston Prakash
 *     
 *
 *******************************************************************************/ 

package hudson.diagnosis;

import hudson.util.TimeUnit2;
import hudson.Extension;
import hudson.model.PeriodicWork;
import hudson.util.ColorPalette;
import hudson.util.graph.MultiStageTimeSeries;
import hudson.util.graph.MultiStageTimeSeries.TimeScale;
import hudson.util.graph.MultiStageTimeSeries.TrendChart;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import org.kohsuke.stapler.QueryParameter;

/**
 * Monitors the memory usage of the system in OS specific way.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public final class MemoryUsageMonitor extends PeriodicWork {
    /**
     * A memory group is conceptually a set of memory pools. 
     */
    public final class MemoryGroup {
        private final List<MemoryPoolMXBean> pools = new ArrayList<MemoryPoolMXBean>();

        /**
         * Trend of the memory usage, after GCs.
         * So this shows the accurate snapshot of the footprint of live objects.
         */
        public final MultiStageTimeSeries used = new MultiStageTimeSeries(Messages._MemoryUsageMonitor_USED(), ColorPalette.RED, 0,0);
        /**
         * Trend of the maximum memory size, after GCs.
         */
        public final MultiStageTimeSeries max = new MultiStageTimeSeries(Messages._MemoryUsageMonitor_TOTAL(), ColorPalette.BLUE, 0,0);

        private MemoryGroup(List<MemoryPoolMXBean> pools, MemoryType type) {
            for (MemoryPoolMXBean pool : pools) {
                if (pool.getType() == type)
                    this.pools.add(pool);
            }
        }

        private void update() {
            long used = 0;
            long max = 0;
//            long cur = 0;
            for (MemoryPoolMXBean pool : pools) {
                MemoryUsage usage = pool.getCollectionUsage();
                if(usage==null) continue;   // not available
                used += usage.getUsed();
                max  += usage.getMax();

//                usage = pool.getUsage();
//                if(usage==null) continue;   // not available
//                cur += usage.getUsed();
            }

            // B -> KB
            used /= 1024;
            max /= 1024;
//            cur /= 1024;

            this.used.update(used);
            this.max.update(max);
//
//            return String.format("%d/%d/%d (%d%%)",used,cur,max,used*100/max);
        }

        /**
         * Generates the memory usage statistics graph.
         */
        public TrendChart doGraph(@QueryParameter String type) throws IOException {
            return MultiStageTimeSeries.createTrendChart(TimeScale.parse(type),used,max);
        }
    }

    public final MemoryGroup heap;
    public final MemoryGroup nonHeap;

    public MemoryUsageMonitor() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        heap = new MemoryGroup(pools, MemoryType.HEAP);
        nonHeap = new MemoryGroup(pools, MemoryType.NON_HEAP);
    }

    public long getRecurrencePeriod() {
        return TimeUnit2.SECONDS.toMillis(10);
    }

    protected void doRun() {
        heap.update();
        nonHeap.update();
    }
}
