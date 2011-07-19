/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.matrix;

import hudson.FilePath;
import hudson.slaves.WorkspaceList;
import hudson.slaves.WorkspaceList.Lease;
import static hudson.matrix.MatrixConfiguration.useShortWorkspaceName;
import hudson.model.Build;
import hudson.model.Node;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Execution of {@link MatrixConfiguration}.
 *
 * @author Kohsuke Kawaguchi
 */
public class MatrixRun extends Build<MatrixConfiguration,MatrixRun> {
    public MatrixRun(MatrixConfiguration job) throws IOException {
        super(job);
    }

    public MatrixRun(MatrixConfiguration job, Calendar timestamp) {
        super(job, timestamp);
    }

    public MatrixRun(MatrixConfiguration project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    @Override
    public String getUpUrl() {
        StaplerRequest req = Stapler.getCurrentRequest();
        if(req!=null) {
            List<Ancestor> ancs = req.getAncestors();
            for( int i=1; i<ancs.size(); i++) {
                if(ancs.get(i).getObject()==this) {
                    Object parentObj = ancs.get(i-1).getObject();
                    if(parentObj instanceof MatrixBuild || parentObj instanceof MatrixConfiguration) {
                        return ancs.get(i-1).getUrl()+'/';
                    }
                }
            }
        }
        return super.getDisplayName();
    }

    /**
     * Gets the {@link MatrixBuild} that has the same build number.
     *
     * @return
     *      null if no such build exists, which happens when the module build
     *      is manually triggered.
     */
    public MatrixBuild getParentBuild() {
        return getParent().getParent().getBuildByNumber(getNumber());
    }
    
    @Override
    public void checkPermission(hudson.security.Permission p) {
        MatrixBuild parentBuild = getParentBuild();
        if(parentBuild != null) {
            parentBuild.checkPermission(p);
        }
    };

    @Override
    public String getDisplayName() {
        StaplerRequest req = Stapler.getCurrentRequest();
        if(req!=null) {
            List<Ancestor> ancs = req.getAncestors();
            for( int i=1; i<ancs.size(); i++) {
                if(ancs.get(i).getObject()==this) {
                    if(ancs.get(i-1).getObject() instanceof MatrixBuild) {
                        return getParent().getCombination().toCompactString(getParent().getParent().getAxes());
                    }
                }
            }
        }
        return super.getDisplayName();
    }

    /**
     * @since 2.1.0
     */
    @Override
    protected void customizeBuildVariables(final Map<String, String> vars) {
        AxisList axes = getParent().getParent().getAxes();
        for (Map.Entry<String,String> e : getParent().getCombination().entrySet()) {
            Axis a = axes.find(e.getKey());
            if (a!=null) {
                a.addBuildVariable(e.getValue(),vars);
            }else {
                vars.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * If the parent {@link MatrixBuild} is kept, keep this record too.
     */
    @Override
    public String getWhyKeepLog() {
        MatrixBuild pb = getParentBuild();
        if(pb!=null && pb.getWhyKeepLog()!=null)
            return Messages.MatrixRun_KeptBecauseOfParent(pb);
        return super.getWhyKeepLog();
    }

    @Override
    public MatrixConfiguration getParent() {// don't know why, but javac wants this
        return super.getParent();
    }

    @Override
    public void run() {
        run(new RunnerImpl());
    }

    protected class RunnerImpl extends Build<MatrixConfiguration,MatrixRun>.RunnerImpl {
        @Override
        protected Lease decideWorkspace(Node n, WorkspaceList wsl) throws InterruptedException, IOException {
            // Map current combination to a directory subtree, e.g. 'axis1=a,axis2=b' to 'axis1/a/axis2/b'.
            String subtree;
            if(useShortWorkspaceName) {
                subtree = getParent().getDigestName(); 
            } else {
                subtree = getParent().getCombination().toString('/','/');
            }
            
            String customWorkspace = getParent().getParent().getCustomWorkspace();
            if (customWorkspace != null) {
                // Use custom workspace as defined in the matrix project settings.
                FilePath ws = n.getRootPath().child(getEnvironment(listener).expand(customWorkspace));
                // We allow custom workspaces to be used concurrently between jobs.
                return Lease.createDummyLease(ws.child(subtree));
            } else {   
                // Use default workspace as assigned by Hudson.
                Node node = getBuiltOn();
                FilePath ws = node.getWorkspaceFor(getParent().getParent());
                // Allocate unique workspace (not to be shared between jobs and runs).
                return wsl.allocate(ws.child(subtree));
            }
        }
    }
}
