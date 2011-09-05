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

package hudson.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.acls.sid.Sid;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import static java.util.logging.Level.FINE;

/**
 * Accses control list.
 *
 * @author Kohsuke Kawaguchi
 */
public class SparseACL extends SidACL {
    public static final class Entry {
        // Sid has value-equality semantics
        //TODO: review and check whether we can do it private
        public final Sid sid;
        public final Permission permission;
        public final boolean allowed;

        public Entry(Sid sid, Permission permission, boolean allowed) {
            this.sid = sid;
            this.permission = permission;
            this.allowed = allowed;
        }

        public Sid getSid() {
            return sid;
        }

        public Permission getPermission() {
            return permission;
        }

        public boolean isAllowed() {
            return allowed;
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();
    private ACL parent;

    public SparseACL(ACL parent) {
        this.parent = parent;
    }

    public void add(Entry e) {
        entries.add(e);
    }

    public void add(Sid sid, Permission permission, boolean allowed) {
        add(new Entry(sid,permission,allowed));
    }

    @Override
    public boolean hasPermission(Authentication a, Permission permission) {
        if(a==SYSTEM)   return true;
        Boolean b = _hasPermission(a,permission);
        if(b!=null) return b;

        if(parent!=null) {
            if(LOGGER.isLoggable(FINE))
                LOGGER.fine("hasPermission("+a+","+permission+") is delegating to parent ACL: "+parent);
            return parent.hasPermission(a,permission);
        }

        // the ultimate default is to reject everything
        return false;
    }

    @Override
    protected Boolean hasPermission(Sid p, Permission permission) {
        for( ; permission!=null; permission=permission.impliedBy ) {
            for (Entry e : entries) {
                if(e.permission==permission && e.sid.equals(p))
                    return e.allowed;
            }
        }
        return null;
    }

    private static final Logger LOGGER = Logger.getLogger(SparseACL.class.getName());
}
