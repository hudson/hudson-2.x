/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.rest.api.build;

import org.eclipse.hudson.rest.api.internal.ConverterSupport;

import org.eclipse.hudson.rest.model.build.TestCaseDTO;
import org.eclipse.hudson.rest.model.build.TestCaseStatusDTO;
import org.eclipse.hudson.rest.model.build.TestSuiteDTO;
import org.eclipse.hudson.rest.model.build.TestsDTO;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.AbstractTestResultAction;

/**
 * Converts {@link AbstractTestResultAction} into {@link TestsDTO} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class TestsConverter
    extends ConverterSupport
{
    public TestsDTO convert(final AbstractTestResultAction<?> source) {
        assert source != null;

        if (source instanceof TestResultAction) {
            return convert((TestResultAction)source);
        }

        log.trace("Converting (ATRA): {}", source);

        TestsDTO target = new TestsDTO();

        target.setTotal(source.getTotalCount());
        target.setFailed(source.getFailCount());
        target.setSkipped(source.getSkipCount());
        
        return target;
    }

    public TestsDTO convert(final TestResultAction source) {
        assert source != null;

        log.trace("Converting (TRA): {}", source);

        TestsDTO target = new TestsDTO();

        target.setTotal(source.getTotalCount());
        target.setFailed(source.getFailCount());
        target.setSkipped(source.getSkipCount());

        TestResult result = source.getResult();
        for (SuiteResult suite : result.getSuites()) {
            target.getSuites().add(convert(suite));
        }
        
        return target;
    }

    public TestSuiteDTO convert(final SuiteResult source) {
        assert source != null;

        log.trace("Converting: {}", source);

        TestSuiteDTO target = new TestSuiteDTO();

        target.setName(source.getName());
        target.setTimeStamp(source.getTimestamp());
        target.setDuration(source.getDuration());
        
        for (CaseResult result : source.getCases()) {
            target.getCases().add(convert(result));
        }
        
        return target;
    }

    public TestCaseDTO convert(final CaseResult source) {
        assert source != null;

        log.trace("Converting: {}", source);

        TestCaseDTO target = new TestCaseDTO();

        target.setName(source.getName());
        target.setClassName(source.getClassName());
        target.setDuration(source.getDuration());
        target.setStatus(convert(source.getStatus()));
        target.setAge(source.getAge());
        target.setErrorDetails(source.getErrorDetails());
        target.setErrorStackTrace(source.getErrorStackTrace());

        return target;
    }

    public TestCaseStatusDTO convert(final CaseResult.Status source) {
        assert source != null;

        log.trace("Converting: {}", source);

        return TestCaseStatusDTO.valueOf(source.name().toUpperCase());
    }
}
