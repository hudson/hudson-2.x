/*******************************************************************************
 *
 * Copyright (c) 2009, Yahoo!, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *      
 *
 *******************************************************************************/ 

package hudson.tasks.junit;

import hudson.model.TaskListener;
import hudson.tasks.test.TestResultParser;
import hudson.model.AbstractBuild;
import hudson.*;
import hudson.remoting.VirtualChannel;

import java.io.IOException;
import java.io.File;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;

/**
 * Parse some JUnit xml files and generate a TestResult containing all the
 * results parsed. 
 */
@Extension
public class JUnitParser extends TestResultParser {

    private final boolean keepLongStdio;

    /** XXX TestResultParser.all does not seem to ever be called so why must this be an Extension? */
    @Deprecated
    public JUnitParser() {
        this(false);
    }

    /**
     * @param keepLongStdio if true, retain a suite's complete stdout/stderr even if this is huge and the suite passed
     * @since 1.358
     */
    public JUnitParser(boolean keepLongStdio) {
        this.keepLongStdio = keepLongStdio;
    }

    @Override
    public String getDisplayName() {
        return "JUnit Parser";
    }

    @Override
    public String getTestResultLocationMessage() {
        return "JUnit xml files:";
    }

    @Override
    public TestResult parse(String testResultLocations,
                                       AbstractBuild build, Launcher launcher,
                                       TaskListener listener)
            throws InterruptedException, IOException
    {
        final long buildTime = build.getTimestamp().getTimeInMillis();
        final long timeOnMaster = System.currentTimeMillis();

        // [BUG 3123310] TODO - Test Result Refactor: review and fix TestDataPublisher/TestAction subsystem]
        // also get code that deals with testDataPublishers from JUnitResultArchiver.perform
        
        TestResult testResult = build.getWorkspace().act( new ParseResultCallable(testResultLocations, buildTime, timeOnMaster, keepLongStdio));
        return testResult;        
    }

    private static final class ParseResultCallable implements
            FilePath.FileCallable<TestResult> {
        private final long buildTime;
        private final String testResults;
        private final long nowMaster;
        private final boolean keepLongStdio;

        private ParseResultCallable(String testResults, long buildTime, long nowMaster, boolean keepLongStdio) {
            this.buildTime = buildTime;
            this.testResults = testResults;
            this.nowMaster = nowMaster;
            this.keepLongStdio = keepLongStdio;
        }

        public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
            final long nowSlave = System.currentTimeMillis();

            FileSet fs = Util.createFileSet(ws, testResults);
            DirectoryScanner ds = fs.getDirectoryScanner();

            String[] files = ds.getIncludedFiles();
            if (files.length == 0) {
                // no test result. Most likely a configuration
                // error or fatal problem
                throw new AbortException(Messages.JUnitResultArchiver_NoTestReportFound());
            }

            TestResult result = new TestResult(buildTime + (nowSlave - nowMaster), ds, keepLongStdio);
            result.tally();
            return result; 
        }
    }

}
