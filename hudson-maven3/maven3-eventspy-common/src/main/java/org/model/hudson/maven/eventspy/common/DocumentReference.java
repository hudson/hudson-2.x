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

package org.model.hudson.maven.eventspy.common;

import java.io.Serializable;

/**
 * Container for access to document details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DocumentReference
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String id;

    private final String content;

    public DocumentReference(final String id, final String content) {
        assert id != null;
        this.id = id;
        assert content != null;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return String.format("(managed-document-id:%s)", getId());
    }
}
