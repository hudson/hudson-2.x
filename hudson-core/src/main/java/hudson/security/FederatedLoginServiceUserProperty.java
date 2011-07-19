/*******************************************************************************
 *
 * Copyright (c) 2010, CloudBees, Inc.
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

package hudson.security;

import hudson.model.UserProperty;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Kohsuke Kawaguchi
 * @since 1.394
 * @see FederatedLoginService
 */
public class FederatedLoginServiceUserProperty extends UserProperty {
    protected final Set<String> identifiers;

    protected FederatedLoginServiceUserProperty(Collection<String> identifiers) {
        this.identifiers = new HashSet<String>(identifiers);
    }

    public boolean has(String identifier) {
        return identifiers.contains(identifier);
    }

    public Collection<String> getIdentifiers() {
        return Collections.unmodifiableSet(identifiers);
    }

    public synchronized void addIdentifier(String id) throws IOException {
        identifiers.add(id);
        user.save();
    }
}
