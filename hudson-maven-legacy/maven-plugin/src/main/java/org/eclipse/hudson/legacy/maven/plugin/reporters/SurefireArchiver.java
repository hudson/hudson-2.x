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
*    Kohsuke Kawaguchi, Jason Chaffee, Maciek Starzyk
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin.reporters;

import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.TestResultProjectAction;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.hudson.legacy.maven.plugin.Maven3Builder;
import org.eclipse.hudson.legacy.maven.plugin.MavenBuild;
import org.eclipse.hudson.legacy.maven.plugin.MavenBuildProxy;
import org.eclipse.hudson.legacy.maven.plugin.MavenBuilder;
import org.eclipse.hudson.legacy.maven.plugin.MavenModule;
import org.eclipse.hudson.legacy.maven.plugin.MavenProjectActionBuilder;
import org.eclipse.hudson.legacy.maven.plugin.MavenReporter;
import org.eclipse.hudson.legacy.maven.plugin.MavenReporterDescriptor;
import org.eclipse.hudson.legacy.maven.plugin.MojoInfo;
import org.eclipse.hudson.legacy.maven.plugin.MavenBuildProxy.BuildCallable;

/**
 * Records the surefire test result.
 * @author Kohsuke Kawaguchi
 */
public class SurefireArchiver extends MavenReporter {
    private TestResult result;

    public boolean preExecute(MavenBuildProxy build, MavenProject pom, MojoInfo mojo, BuildListener listener) throws InterruptedException, IOException {
        if (isSurefireTest(mojo)) {
            if (!mojo.is("org.apache.maven.plugins", "maven-failsafe-plugin", "integration-test")) {
                // tell surefire:test to keep going even if there was a failure,
                // so that we can record this as yellow.
                // note that because of the way Maven works, just updating system property at this point is too late
                XmlPlexusConfiguration c = (XmlPlexusConfiguration) mojo.configuration.getChild("testFailureIgnore");
                if(c!=null && c.getValue().equals("${maven.test.failure.ignore}") && System.getProperty("maven.test.failure.ignore")==null) {
                    if (maven3orLater( build.getMavenBuildInformation().getMavenVersion() )) {
                        String fieldName = "testFailureIgnore";
                        if (mojo.mojoExecution.getConfiguration().getChild( fieldName ) != null) {
                          mojo.mojoExecution.getConfiguration().getChild( fieldName ).setValue( Boolean.TRUE.toString() );
                        } else {
                            Xpp3Dom child = new Xpp3Dom( fieldName );
                            child.setValue( Boolean.TRUE.toString() );
                            mojo.mojoExecution.getConfiguration().addChild( child );
                        }
                        
                    } else {
                        c.setValue(Boolean.TRUE.toString());
                    }
                }
            }
        }
        return true;
    }

    public boolean postExecute(MavenBuildProxy build, MavenProject pom, MojoInfo mojo, final BuildListener listener, Throwable error) throws InterruptedException, IOException {
        if (!isSurefireTest(mojo)) return true;

        listener.getLogger().println(Messages.SurefireArchiver_Recording());

        File reportsDir;
        if (mojo.is("org.apache.maven.plugins", "maven-surefire-plugin", "test") ||
            mojo.is("org.apache.maven.plugins", "maven-failsafe-plugin", "integration-test")) {
            try {
                reportsDir = mojo.getConfigurationValue("reportsDirectory", File.class);
            } catch (ComponentConfigurationException e) {
                e.printStackTrace(listener.fatalError(Messages.SurefireArchiver_NoReportsDir()));
                build.setResult(Result.FAILURE);
                return true;
            }
        }
        else {
            reportsDir = new File(pom.getBasedir(), "target/surefire-reports");
        }

        if(reportsDir.exists()) {
            // surefire:test just skips itself when the current project is not a java project

            FileSet fs = Util.createFileSet(reportsDir,"*.xml","testng-results.xml,testng-failed.xml");
            DirectoryScanner ds = fs.getDirectoryScanner();

            if(ds.getIncludedFiles().length==0)
                // no test in this module
                return true;

            if(result==null)    result = new TestResult();
            result.parse(System.currentTimeMillis() - build.getMilliSecsSinceBuildStart(), ds);

            int failCount = build.execute(new BuildCallable<Integer, IOException>() {
                public Integer call(MavenBuild build) throws IOException, InterruptedException {
                    SurefireReport sr = build.getAction(SurefireReport.class);
                    if(sr==null)
                        build.getActions().add(new SurefireReport(build, result, listener));
                    else
                        sr.setResult(result,listener);
                    if(result.getFailCount()>0)
                        build.setResult(Result.UNSTABLE);
                    build.registerAsProjectAction(new FactoryImpl());
                    return result.getFailCount();
                }
            });

            // if surefire plugin is going to kill maven because of a test failure,
            // intercept that (or otherwise build will be marked as failure)
            if(failCount>0 && error instanceof MojoFailureException) {
                MavenBuilder.markAsSuccess = true;
            }
            // TODO currenlty error is empty : will be here with maven 3.0.2+
            if(failCount>0) {
                Maven3Builder.markAsSuccess = true;
            }            
        }

        return true;
    }

    /**
     * Up to 1.372, there was a bug that causes Hudson to persist {@link SurefireArchiver} with the entire test result
     * in it. If we are loading those, fix it up in memory to reduce the memory footprint.
     *
     * It'd be nice we can save the record to remove problematic portion, but that might have
     * additional side effect.
     */
    public static void fixUp(List<MavenProjectActionBuilder> builders) {
        if (builders==null) return;
        for (ListIterator<MavenProjectActionBuilder> itr = builders.listIterator(); itr.hasNext();) {
            MavenProjectActionBuilder b =  itr.next();
            if (b instanceof SurefireArchiver)
                itr.set(new FactoryImpl());
        }
    }

    /**
     * Part of the serialization data attached to {@link MavenBuild}.
     */
    public static final class FactoryImpl implements MavenProjectActionBuilder {
        public Collection<? extends Action> getProjectActions(MavenModule module) {
            return Collections.singleton(new TestResultProjectAction(module));
        }
    }

    private boolean isSurefireTest(MojoInfo mojo) {
        if ((!mojo.is("com.sun.maven", "maven-junit-plugin", "test"))
            && (!mojo.is("org.sonatype.flexmojos", "flexmojos-maven-plugin", "test-run"))
            && (!mojo.is("org.apache.maven.plugins", "maven-surefire-plugin", "test"))
            && (!mojo.is("org.apache.maven.plugins", "maven-failsafe-plugin", "integration-test")))
            return false;

        try {
            if (mojo.is("org.apache.maven.plugins", "maven-surefire-plugin", "test")) {
                Boolean skip = mojo.getConfigurationValue("skip", Boolean.class);
                if (((skip != null) && (skip))) {
                    return false;
                }
                
                if (mojo.pluginName.version.compareTo("2.3") >= 0) {
                    Boolean skipExec = mojo.getConfigurationValue("skipExec", Boolean.class);
                    
                    if (((skipExec != null) && (skipExec))) {
                        return false;
                    }
                }
                
                if (mojo.pluginName.version.compareTo("2.4") >= 0) {
                    Boolean skipTests = mojo.getConfigurationValue("skipTests", Boolean.class);
                    
                    if (((skipTests != null) && (skipTests))) {
                        return false;
                    }
                }
            }
            else if (mojo.is("com.sun.maven", "maven-junit-plugin", "test")) {
                Boolean skipTests = mojo.getConfigurationValue("skipTests", Boolean.class);
                
                if (((skipTests != null) && (skipTests))) {
                    return false;
                }
            }
            else if (mojo.is("org.sonatype.flexmojos", "flexmojos-maven-plugin", "test-run")) {
		Boolean skipTests = mojo.getConfigurationValue("skipTest", Boolean.class);
		
		if (((skipTests != null) && (skipTests))) {
		    return false;
		}
	    }

        } catch (ComponentConfigurationException e) {
            return false;
        }

        return true;
    }
    
    public boolean maven3orLater(String mavenVersion) {
        // null or empty so false !
        if (StringUtils.isBlank( mavenVersion )) {
            return false;
        }
        return new ComparableVersion (mavenVersion).compareTo( new ComparableVersion ("3.0") ) >= 0;
    }       

    @Extension
    public static final class DescriptorImpl extends MavenReporterDescriptor {
        public String getDisplayName() {
            return Messages.SurefireArchiver_DisplayName();
        }

        public SurefireArchiver newAutoInstance(MavenModule module) {
            return new SurefireArchiver();
        }
    }

    private static final long serialVersionUID = 1L;
}
