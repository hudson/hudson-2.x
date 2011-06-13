/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.rest.api.build;

import org.hudsonci.service.SystemService;
import hudson.model.ItemGroup;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;

import java.util.Calendar;
import java.util.Iterator;

import org.hudsonci.rest.api.build.BuildConverter;
import org.hudsonci.rest.api.build.CauseConverter;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.hudsonci.rest.model.build.BuildDTO;
import org.hudsonci.rest.model.build.BuildStateDTO;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class BuildConverterTest
{


    @Mock
    SystemService systemService;

    @Mock
    CauseConverter causex;

    @Test
    public void checkChangesDetected()
    {
        AbstractBuild source = createMockSource();

        ChangeLogSet changeset = Mockito.mock( ChangeLogSet.class );
        Iterator iterator = Mockito.mock( Iterator.class );
        Mockito.when( changeset.iterator() ).thenReturn( iterator );
        Mockito.when( source.getChangeSet() ).thenReturn( changeset );

        BuildConverter bc = new BuildConverter(systemService,causex);
        BuildDTO build = bc.convert( source );
        assertTrue( build.isChangesAvailable() );
    }

    @Test
    public void checkNoChangesDetectedWhenEmptySet()
    {
        AbstractBuild source = createMockSource();

        ChangeLogSet changeset = Mockito.mock( ChangeLogSet.class );
        Mockito.when( changeset.isEmptySet() ).thenReturn( true );
        Iterator iterator = Mockito.mock( Iterator.class );
        Mockito.when( changeset.iterator() ).thenReturn( iterator );
        Mockito.when( source.getChangeSet() ).thenReturn( changeset );

        BuildConverter bc = new BuildConverter(systemService,causex);
        BuildDTO build = bc.convert( source );
        assertFalse( build.isChangesAvailable() );
    }

    @Ignore("FIXME: AbstractBuild will not provide null changeset")
    @Test
    public void checkNoChangesDetectedWhenNullSet()
    {
        AbstractBuild source = createMockSource();
        Mockito.when( source.getChangeSet() ).thenReturn( (ChangeLogSet) null );

        BuildConverter bc = new BuildConverter(systemService,causex);
        BuildDTO build = bc.convert( source );
        assertFalse( build.isChangesAvailable() );
    }

    // TODO: null source.getUrl()
    // TODO: null source.getChangeSet() - bug

    @Test
    public void checkTestsDetected()
    {
        AbstractBuild source = createMockSource();

        // unrelated, but changes have to exist else code will throw an NPE
        ChangeLogSet changeset = Mockito.mock( ChangeLogSet.class );
        Iterator iterator = Mockito.mock( Iterator.class );
        Mockito.when( changeset.iterator() ).thenReturn( iterator );
        Mockito.when( source.getChangeSet() ).thenReturn( changeset );

        AbstractTestResultAction testResultAction = Mockito.mock( AbstractTestResultAction.class );
        Mockito.when( testResultAction.getTotalCount() ).thenReturn( 1 );
        Mockito.when( source.getTestResultAction() ).thenReturn( testResultAction );

        BuildConverter bc = new BuildConverter(systemService,causex);
        BuildDTO build = bc.convert( source );
        assertTrue( build.isTestsAvailable() );
    }

    @Test
    public void checkNoTestsDetectedWhenResultsAreEmpty()
    {
        AbstractBuild source = createMockSource();

        // unrelated, but changes have to exist else code will throw an NPE
        ChangeLogSet changeset = Mockito.mock( ChangeLogSet.class );
        Iterator iterator = Mockito.mock( Iterator.class );
        Mockito.when( changeset.iterator() ).thenReturn( iterator );
        Mockito.when( source.getChangeSet() ).thenReturn( changeset );

        AbstractTestResultAction testResultAction = Mockito.mock( AbstractTestResultAction.class );
        Mockito.when( testResultAction.getTotalCount() ).thenReturn( 0 );
        Mockito.when( source.getTestResultAction() ).thenReturn( testResultAction );

        BuildConverter bc = new BuildConverter(systemService,causex);
        BuildDTO build = bc.convert( source );
        assertFalse( build.isTestsAvailable() );
    }

    @Test
    public void checkNoTestsDetectedWhenResultsAreNull()
    {
        AbstractBuild source = createMockSource();

        // unrelated, but changes have to exist else code will throw an NPE
        ChangeLogSet changeset = Mockito.mock( ChangeLogSet.class );
        Iterator iterator = Mockito.mock( Iterator.class );
        Mockito.when( changeset.iterator() ).thenReturn( iterator );
        Mockito.when( source.getChangeSet() ).thenReturn( changeset );

        Mockito.when( source.getTestResultAction() ).thenReturn( null );

        BuildConverter bc = new BuildConverter(systemService,causex);
        BuildDTO build = bc.convert( source );
        assertFalse( build.isTestsAvailable() );
    }

    @Test
    public void checkBuildStateCompletedIfNotStartedBuildingOrLogging()
    {
        AbstractBuild source = createMockSource();

        Mockito.when( source.hasntStartedYet() ).thenReturn( false );
        Mockito.when( source.isBuilding() ).thenReturn( false );
        Mockito.when( source.isLogUpdated() ).thenReturn( false );

        // unrelated, but changes have to exist else code will throw an NPE
        ChangeLogSet changeset = Mockito.mock( ChangeLogSet.class );
        Iterator iterator = Mockito.mock( Iterator.class );
        Mockito.when( changeset.iterator() ).thenReturn( iterator );
        Mockito.when( source.getChangeSet() ).thenReturn( changeset );

        BuildConverter bc = new BuildConverter(systemService,causex);
        BuildDTO build = bc.convert( source );
        assertEquals( BuildStateDTO.COMPLETED, build.getState() );
    }

    private AbstractBuild createMockSource()
    {
        AbstractBuild source = Mockito.mock( AbstractBuild.class );
        AbstractProject project = Mockito.mock( AbstractProject.class );
        ItemGroup parent = Mockito.mock( ItemGroup.class );
        Mockito.when( parent.getFullName() ).thenReturn( "dummy itemgroup" );
        Mockito.when( project.getParent() ).thenReturn( parent );
        Mockito.when( project.getFullName() ).thenReturn( "dummy project" );
        Mockito.when( source.getProject() ).thenReturn( project );

        Calendar timestamp = Mockito.mock( Calendar.class );
        Mockito.when( source.getTimestamp() ).thenReturn( timestamp );

        return source;
    }

}
