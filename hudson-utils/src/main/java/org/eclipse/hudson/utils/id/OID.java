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

package org.eclipse.hudson.utils.id;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a general identifier for objects.
 *
 * String representation should follow the default behavior of {@link Object#toString}.
 * {@link #hashCode}/{@link #equals} behavior differs to make the OID unique based on
 * {@link #type} and {@link #hash}.  When constructing from an object the hash is always
 * the {@link System#identityHashCode}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@XStreamAlias("oid")
public class OID
    implements ID
{
    public static final OID NULL = new OID();

    @XStreamAsAttribute
    private final String type;

    @XStreamAsAttribute
    private final int hash;

    private OID(final String type, final int hash) {
        this.type = checkNotNull(type);
        this.hash = hash;
    }

    private OID() {
        type = null;
        hash = System.identityHashCode(this);
    }

    public String getType() {
        return type;
    }

    public int getHash() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OID)) {
            return false;
        }

        OID that = (OID) obj;

        if (hash != that.hash) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + hash;
        return result;
    }

    @Override
    public String toString() {
        if (this == NULL) {
            return null;
        }
        return String.format("%s@%x", type, hash);
    }

    public static OID get(final Object obj) {
        if (obj == null) {
            return NULL;
        }
        return new OID(obj.getClass().getName(), System.identityHashCode(obj));
    }

    /**
     * @see #get
     */
    public static OID oid(final Object obj) {
        return get(obj);
    }

    public static OID parse(final String spec) {
        assert spec != null;
        String[] items = spec.split("@");
        if (items.length != 2) {
            throw new IllegalArgumentException();
        }
        return new OID(items[0], Integer.parseInt(items[1], 16));
    }

    public static String render(final Object obj) {
        assert obj != null;
        return get(obj).toString();
    }

    public static <T> T find(final Collection<T> items, final String id) {
        assert items != null;
        assert id != null;

        for (T item : items) {
            if (OID.render(item).equals(id)) {
                return item;
            }
        }

        return null;
    }

    public static <T> T find(final Collection<T> items, final OID id) {
        assert id != null;
        return find(items, id.toString());
    }
}
