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
 *    Inc., Kohsuke Kawaguchi, Daniel Dyer,    Red Hat, Inc., Stephen Connolly, id:cactusman, Yahoo!, Inc, Winston Prakash
 *     
 *
 *******************************************************************************/ 

package hudson.tasks.test;

import hudson.util.graph.DataSet;
import hudson.Functions;
import hudson.model.*;
import hudson.tasks.junit.CaseResult;
import hudson.util.*;
import hudson.util.graph.ChartLabel;
import hudson.util.graph.ChartUtil;
import hudson.util.graph.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.graph.Graph;
import java.awt.Color;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Common base class for recording test result.
 *
 * <p>
 * {@link Project} and {@link Build} recognizes {@link Action}s that derive from this,
 * and displays it nicely (regardless of the underlying implementation.)
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public abstract class AbstractTestResultAction<T extends AbstractTestResultAction> implements HealthReportingAction {
    //TODO: review and check whether we can do it private
    public final AbstractBuild<?,?> owner;

    private Map<String,String> descriptions = new ConcurrentHashMap<String, String>();

    protected AbstractTestResultAction(AbstractBuild owner) {
        this.owner = owner;
    }

    /**
     * Gets the number of failed tests.
     */
    @Exported(visibility=2)
    public abstract int getFailCount();

    /**
     * Gets the number of skipped tests.
     */
    @Exported(visibility=2)
    public int getSkipCount() {
        // Not all sub-classes will understand the concept of skipped tests.
        // This default implementation is for them, so that they don't have
        // to implement it (this avoids breaking existing plug-ins - i.e. those
        // written before this method was added in 1.178).
        // Sub-classes that do support skipped tests should over-ride this method.
        return 0;
    }

    /**
     * Gets the total number of tests.
     */
    @Exported(visibility=2)
    public abstract int getTotalCount();

    /**
     * Gets the diff string of failures.
     */
    public final String getFailureDiffString() {
        T prev = getPreviousResult();
        if(prev==null)  return "";  // no record

        return " / "+Functions.getDiffString(this.getFailCount()-prev.getFailCount());
    }

    public String getDisplayName() {
        return Messages.AbstractTestResultAction_getDisplayName();
    }

    @Exported(visibility=2)
    public String getUrlName() {
        return "testReport";
    }

    public String getIconFileName() {
        return "clipboard.gif";
    }

    public AbstractBuild getOwner() {
        return owner;
    }

    public HealthReport getBuildHealth() {
        final int totalCount = getTotalCount();
        final int failCount = getFailCount();
        int score = (totalCount == 0) ? 100 : (int) (100.0 * (1.0 - ((double)failCount) / totalCount));
        Localizable description, displayName = Messages._AbstractTestResultAction_getDisplayName();
        if (totalCount == 0) {
        	description = Messages._AbstractTestResultAction_zeroTestDescription(displayName);
        } else {
        	description = Messages._AbstractTestResultAction_TestsDescription(displayName, failCount, totalCount);
        }
        return new HealthReport(score, description);
    }

    /**
     * Exposes this object to the remote API.
     */
    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns the object that represents the actual test result.
     * This method is used by the remote API so that the XML/JSON
     * that we are sending won't contain unnecessary indirection
     * (that is, {@link AbstractTestResultAction} in between.
     *
     * <p>
     * If such a concept doesn't make sense for a particular subtype,
     * return <tt>this</tt>.
     */
    public abstract Object getResult();

    /**
     * Gets the test result of the previous build, if it's recorded, or null.
     */
    public T getPreviousResult() {
        return (T)getPreviousResult(getClass());
    }

    private <U extends AbstractTestResultAction> U getPreviousResult(Class<U> type) {
        AbstractBuild<?,?> b = owner;
        while(true) {
            b = b.getPreviousBuild();
            if(b==null)
                return null;
            U r = b.getAction(type);
            if(r!=null)
                return r;
        }
    }
    
    public TestResult findPreviousCorresponding(TestResult test) {
        T previousResult = getPreviousResult();
        if (previousResult != null) {
            TestResult testResult = (TestResult)getResult();
            return testResult.findCorrespondingResult(test.getId());
        }

        return null;
    }

    public TestResult findCorrespondingResult(String id) {
        return ((TestResult)getResult()).findCorrespondingResult(id);
    }
    
    /**
     * A shortcut for summary.jelly
     * 
     * @return List of failed tests from associated test result.
     */
    public List<CaseResult> getFailedTests() {
        return Collections.emptyList();
    }

    /**
     * Generates a PNG image for the test result trend.
     */
    public void doGraph( StaplerRequest req, StaplerResponse rsp) throws IOException {
        if(ChartUtil.awtProblemCause!=null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath()+"/images/headless.png");
            return;
        }

        if(req.checkIfModified(owner.getTimestamp(),rsp))
            return;
    
        Area defSize = calcDefaultSize();
        Graph graph = new Graph(-1, defSize.width, defSize.height);
        graph.setXAxisLabel("count");
        graph.setData(getGraphDataSet(req));
        graph.doPng(req,rsp);
        
        //ChartUtil.generateGraph(req,rsp,createChart(req,buildDataSet(req)),calcDefaultSize());
    }

    /**
     * Generates a clickable map HTML for {@link #doGraph(StaplerRequest, StaplerResponse)}.
     */
    public void doGraphMap( StaplerRequest req, StaplerResponse rsp) throws IOException {
        if(req.checkIfModified(owner.getTimestamp(),rsp))
            return;
        
        Area defSize = calcDefaultSize();
        Graph graph = new Graph(-1, defSize.width, defSize.height);
        graph.setXAxisLabel("count");
        graph.setData(getGraphDataSet(req));
        graph.doMap(req,rsp);
    }
    
    private DataSet getGraphDataSet(StaplerRequest req) {
        boolean failureOnly = Boolean.valueOf(req.getParameter("failureOnly"));

        DataSet<String, ChartLabel> dsb = new DataSet<String, ChartLabel>();

        for( AbstractTestResultAction<?> a=this; a!=null; a=a.getPreviousResult(AbstractTestResultAction.class) ) {
            dsb.add( a.getFailCount(), "failed", new TestResultChartLabel(req, a.owner));
            if(!failureOnly) {
                
                dsb.add( a.getSkipCount(), "skipped", new TestResultChartLabel(req, a.owner));
                dsb.add( a.getTotalCount()-a.getFailCount()-a.getSkipCount(),"total", new TestResultChartLabel(req, a.owner));
            }
        }
        return dsb;
    }
    
    private class TestResultChartLabel extends NumberOnlyBuildLabel{
        final String relPath;
        
        public TestResultChartLabel(StaplerRequest req, AbstractBuild build){
            super(build);
            relPath = getRelPath(req);
        }
        @Override
        public Color getColor(int row, int column) {
            return Color.BLUE;
        }

        @Override
        public String getLink(int row, int column) {
            return relPath + build.getNumber()+"/testReport/";
        }

        @Override
        public String getToolTip(int row, int column) {
             
                AbstractTestResultAction a = build.getAction(AbstractTestResultAction.class);
                switch (row) {
                    case 0:
                        return String.valueOf(Messages.AbstractTestResultAction_fail(build.getDisplayName(), a.getFailCount()));
                    case 1:
                        return String.valueOf(Messages.AbstractTestResultAction_skip(build.getDisplayName(), a.getSkipCount()));
                    default:
                        return String.valueOf(Messages.AbstractTestResultAction_test(build.getDisplayName(), a.getTotalCount()));
                }
        }

        public int compareTo(ChartLabel t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * Returns a full path down to a test result
     */
    public String getTestResultPath(TestResult it) {
        return getUrlName() + "/" + it.getRelativePathFrom(null);
    }

    /**
     * Determines the default size of the trend graph.
     *
     * This is default because the query parameter can choose arbitrary size.
     * If the screen resolution is too low, use a smaller size.
     */
    private Area calcDefaultSize() {
        Area res = Functions.getScreenResolution();
        if(res!=null && res.width<=800)
            return new Area(250,100);
        else
            return new Area(500,200);
    }

    private String getRelPath(StaplerRequest req) {
        String relPath = req.getParameter("rel");
        if(relPath==null)   return "";
        return relPath;
    }

    /**
     * {@link TestObject}s do not have their own persistence mechanism, so updatable data of {@link TestObject}s
     * need to be persisted by the owning {@link AbstractTestResultAction}, and this method and
     * {@link #setDescription(TestObject, String)} provides that logic.
     *
     * <p>
     * The default implementation stores information in the 'this' object.
     *
     * @see TestObject#getDescription() 
     */
    protected String getDescription(TestObject object) {
    	return descriptions.get(object.getId());
    }

    protected void setDescription(TestObject object, String description) {
    	descriptions.put(object.getId(), description);
    }

    public Object readResolve() {
    	if (descriptions == null) {
    		descriptions = new ConcurrentHashMap<String, String>();
    	}
    	
    	return this;
    }
}
