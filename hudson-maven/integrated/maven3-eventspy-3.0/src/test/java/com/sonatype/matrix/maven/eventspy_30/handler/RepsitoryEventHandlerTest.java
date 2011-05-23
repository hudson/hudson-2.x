/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30.handler;

import com.sonatype.matrix.maven.model.state.ArtifactOperationDTO;

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
