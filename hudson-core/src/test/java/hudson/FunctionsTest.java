/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    
 * Anton Kozak
 *
 *******************************************************************************/ 

package hudson;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

/**
 * Test for {@link Functions}
 * <p/>
 * Date: 5/19/11
 *
 * @author Anton Kozak
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class FunctionsTest {
    private static final String USER = "admin";

    @Test
    public void testIsAuthorTrue() throws Exception {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(user.getId()).andReturn(USER);
        expect(User.current()).andReturn(user);
        Job job = createMock(FreeStyleProject.class);
        expect(job.getCreatedBy()).andReturn(USER).times(2);
        replay(User.class, user, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, user, job);
        assertTrue(result);
    }

    @Test
    public void testIsAuthorFalse() throws Exception {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(user.getId()).andReturn(USER);
        expect(User.current()).andReturn(user);
        Job job = createMock(FreeStyleProject.class);
        expect(job.getCreatedBy()).andReturn("user").times(2);
        replay(User.class, user, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, user, job);
        assertFalse(result);
    }

    @Test
    public void testIsAuthorJobCreatedByNull() throws Exception {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(User.current()).andReturn(user);
        Job job = createMock(FreeStyleProject.class);
        expect(job.getCreatedBy()).andReturn(null);
        replay(User.class, user, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, user, job);
        assertFalse(result);
    }

    @Test
    public void testIsAuthorUserIdNull() throws Exception {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(user.getId()).andReturn(null);
        expect(User.current()).andReturn(user);
        Job job = createMock(FreeStyleProject.class);
        expect(job.getCreatedBy()).andReturn(USER).times(2);
        replay(User.class, user, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, user, job);
        assertFalse(result);
    }

    @Test
    public void testIsAuthorUserNull() throws Exception {
        mockStatic(User.class);
        expect(User.current()).andReturn(null);
        Job job = createMock(FreeStyleProject.class);
        replay(User.class, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, job);
        assertFalse(result);
    }
}
