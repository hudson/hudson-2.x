/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Inc., Kohsuke Kawaguchi, Seiji Sogabe, Winston Prakash
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
package hudson.node_monitors;

import hudson.Util;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.util.jna.NativeAccessException;
import hudson.util.jna.NativeFunction;
import hudson.util.jna.NativeUtils;
import hudson.util.jna.NativeSystemMemory;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;

/**
 * Checks the swap space availability.
 *
 * @author Kohsuke Kawaguchi
 * @sine 1.233
 */
public class SwapSpaceMonitor extends NodeMonitor {
    /**
     * Returns the HTML representation of the space.
     */
    public String toHtml(NativeSystemMemory usage) {
        if(usage.getAvailableSwapSpace() == -1)
            return "N/A";

        long free = usage.getAvailableSwapSpace();
        free/=1024L;   // convert to KB
        free/=1024L;   // convert to MB
        if(free>256 || usage.getTotalSwapSpace() < usage.getAvailableSwapSpace() * 5)
            return free+"MB"; // if we have more than 256MB free or less than 80% filled up, it's OK

        // Otherwise considered dangerously low.
        return Util.wrapToErrorSpan(free+"MB");
    }

    public long toMB(NativeSystemMemory usage) {
        if(usage.getAvailableSwapSpace() == -1)
            return -1;

        long free = usage.getAvailableSwapSpace();
        free/=1024L;   // convert to KB
        free/=1024L;   // convert to MB
        return free;
    }

    @Override
    public String getColumnCaption() {
        // Hide this column from non-admins
        return Hudson.getInstance().hasPermission(Hudson.ADMINISTER) ? super.getColumnCaption() : null;
    }

    @Extension
    public static final AbstractNodeMonitorDescriptor<NativeSystemMemory> DESCRIPTOR = new AbstractNodeMonitorDescriptor<NativeSystemMemory>() {
        protected NativeSystemMemory monitor(Computer c) throws IOException, InterruptedException {
            return c.getChannel().call(new MonitorTask());
        }

        public String getDisplayName() {
            return Messages.SwapSpaceMonitor_DisplayName();
        }

        @Override
        public NodeMonitor newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new SwapSpaceMonitor();
        }
    };

    /**
     * Obtains the string that represents the architecture.
     */
    private static class MonitorTask implements Callable<NativeSystemMemory, IOException> {

        private static final long serialVersionUID = 1L;
        private static boolean warned = false;

        public NativeSystemMemory call() throws IOException {

            try {
                return new MemoryUsage(NativeUtils.getInstance().getSystemMemory());
            } catch (NativeAccessException exc) {
                if (!warned) {
                    // report the problem just once, and avoid filling up the log with the same error. see HUDSON-2194.
                    warned = true;
                    throw new IOException(exc);
                } else {
                    return null;
                }
            }

        }
    }

    /**
     * Memory Usage.
     *
     * <p>
     * {@link MemoryUsage} + stapler annotations.
     */
    @ExportedBean
    public static class MemoryUsage implements NativeSystemMemory{
        
        NativeSystemMemory systemMemory;
        
        public MemoryUsage(NativeSystemMemory mem) {
            systemMemory = mem; 
        }

        /**
         * Total physical memory of the system, in bytes.
         */
        @Exported
        public long getTotalPhysicalMemory() {
            return systemMemory.getTotalPhysicalMemory();
        }

        /**
         * Of the total physical memory of the system, available bytes.
         */
        @Exported
        public long getAvailablePhysicalMemory() {
            return systemMemory.getAvailablePhysicalMemory();
        }

        /**
         * Total number of swap space in bytes.
         */
        @Exported
        public long getTotalSwapSpace() {
            return systemMemory.getTotalSwapSpace();
        }

        /**
         * Available swap space in bytes.
         */
        @Exported
        public long getAvailableSwapSpace() {
            return systemMemory.getAvailableSwapSpace();
        }
    }
}
