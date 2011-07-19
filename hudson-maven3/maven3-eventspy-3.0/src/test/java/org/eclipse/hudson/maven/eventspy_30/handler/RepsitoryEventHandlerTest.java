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

package org.eclipse.hudson.maven.eventspy_30.handler;

import org.eclipse.hudson.maven.eventspy_30.handler.RepositoryEventHandler;
import org.eclipse.hudson.maven.model.state.ArtifactOperationDTO;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.aether.RepositoryEvent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link RepositoryEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RepsitoryEventHandlerTest
{
    @Mock
    private RepositoryEvent event;
    
    @Test
    public void aetherFailedResolutionsAreTransformedToNotFoundOperations() throws Exception
    {
        when( event.getFile() ).thenReturn( null );
        assertThat( new RepositoryEventHandler().resolveOperationType( event ), equalTo( ArtifactOperationDTO.NOT_FOUND ) );
    }
}
