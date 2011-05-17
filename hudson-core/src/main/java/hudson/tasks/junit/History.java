/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Tom Huybrechts, Yahoo!, Inc., Seiji Sogabe, Winston Prakash
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
package hudson.tasks.junit;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;
import hudson.util.ColorPalette;
import hudson.util.graph.ChartLabel;
import hudson.util.graph.DataSet;
import hudson.util.graph.Graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.Stapler;

/**
 * History of {@link hudson.tasks.test.TestObject} over time.
 *
 * @since 1.320
 */
public class History {
	private final TestObject testObject;

	public History(TestObject testObject) {
		this.testObject = testObject;
	}

	public TestObject getTestObject() {
		return testObject;
	}
	
    public boolean historyAvailable() {
       if (testObject.getOwner().getParent().getBuilds().size() > 1)
           return true;
        else
           return false; 
    }
	
    public List<TestResult> getList(int start, int end) {
    	List<TestResult> list = new ArrayList<TestResult>();
    	end = Math.min(end, testObject.getOwner().getParent().getBuilds().size());
    	for (AbstractBuild<?,?> b: testObject.getOwner().getParent().getBuilds().subList(start, end)) {
    		if (b.isBuilding()) continue;
    		TestResult o = testObject.getResultInBuild(b);
    		if (o != null) {
    			list.add(o);
    		}
    	}
    	return list;
    }
    
	public List<TestResult> getList() {
		return getList(0, testObject.getOwner().getParent().getBuilds().size());
	}

    /**
     * Graph of duration of tests over time.
     */
    public Graph getDurationGraph() {
        Graph graph = new Graph(-1, 600, 300);
        graph.setXAxisLabel("seconds");
        graph.setData(getDurationGraphDataSet());
        return graph;
    }
    
    private DataSet<String, HistoryChartLabel> getDurationGraphDataSet() {
        DataSet<String, HistoryChartLabel> data = new DataSet<String, HistoryChartLabel>();

        List<TestResult> list;
        try {
            list = getList(
                    Integer.parseInt(Stapler.getCurrentRequest().getParameter("start")),
                    Integer.parseInt(Stapler.getCurrentRequest().getParameter("end")));
        } catch (NumberFormatException e) {
            list = getList();
        }

        for (hudson.tasks.test.TestResult o : list) {
            data.add(((double) o.getDuration()) / (1000), "", new HistoryChartLabel(o) {

                @Override
                public Color getColor(int row, int column) {
                    if (o.getFailCount() > 0) {
                        return ColorPalette.RED;
                    } else if (o.getSkipCount() > 0) {
                        return ColorPalette.YELLOW;
                    } else {
                        return ColorPalette.BLUE;
                    }
                }
            });
        }
        return data;
    }

    /**
     * Graph of # of tests over time.
     */
    public Graph getCountGraph() {
        Graph graph = new Graph(-1, 600, 300);
        graph.setXAxisLabel("");
        graph.setData(getCountGraphDataSet());
        return graph; 
    }
    
    private DataSet<String, HistoryChartLabel> getCountGraphDataSet() {
        DataSet<String, HistoryChartLabel> data = new DataSet<String, HistoryChartLabel>();

        List<TestResult> list;
        try {
            list = getList(
                    Integer.parseInt(Stapler.getCurrentRequest().getParameter("start")),
                    Integer.parseInt(Stapler.getCurrentRequest().getParameter("end")));
        } catch (NumberFormatException e) {
            list = getList();
        }

        for (TestResult o : list) {
            data.add(o.getPassCount(), "2Passed", new HistoryChartLabel(o));
            data.add(o.getFailCount(), "1Failed", new HistoryChartLabel(o));
            data.add(o.getSkipCount(), "0Skipped", new HistoryChartLabel(o));
        }
        return data;
    }

    class HistoryChartLabel extends ChartLabel {
    	TestResult o;
        String url;
        public HistoryChartLabel(TestResult o) {
            this.o = o;
            this.url = null;
        }

         private void generateUrl() {
            AbstractBuild<?,?> build = o.getOwner();
            String buildLink = build.getUrl();
            String actionUrl = o.getTestResultAction().getUrlName();
            this.url = Hudson.getInstance().getRootUrl() + buildLink + actionUrl + o.getUrl();             
        }

        public int compareTo(ChartLabel that) {
            return this.o.getOwner().number - ((HistoryChartLabel)that).o.getOwner().number;
        }

        @Override
        public boolean equals(Object o) {
        	if (!(o instanceof HistoryChartLabel)) {
            	return false;
            }
            HistoryChartLabel that = (HistoryChartLabel) o;
            return this.o == that.o;
        }

        @Override
        public int hashCode() {
            return o.hashCode();
        }

        @Override
        public String toString() {
            String l = o.getOwner().getDisplayName();
            String s = o.getOwner().getBuiltOnStr();
            if (s != null)
                l += ' ' + s;
            return l;
//            return o.getDisplayName() + " " + o.getOwner().getDisplayName();
        }

        @Override
        public Color getColor(int row, int column) {
            return null;
        }

        @Override
        public String getLink(int row, int column) {
            if (this.url == null) generateUrl();
            return url;
        }

        @Override
        public String getToolTip(int row, int column) {
            return o.getOwner().getDisplayName() + " : " + o.getDurationString();
        }
    }

}
