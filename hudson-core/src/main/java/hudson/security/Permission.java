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
*    Kohsuke Kawaguchi, Yahoo! Inc.
 *     
 *
 *******************************************************************************/ 

package hudson.security;

import hudson.model.*;
import net.sf.json.util.JSONUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jvnet.localizer.Localizable;

/**
 * Permission, which represents activity that requires a security privilege.
 *
 * <p>
 * Each permission is represented by a specific instance of {@link Permission}.
 *
 * @author Kohsuke Kawaguchi
 * @see http://wiki.hudson-ci.org/display/HUDSON/Making+your+plugin+behave+in+secured+Hudson
 */
public final class Permission {

    /**
     * Comparator that orders {@link Permission} objects based on their ID.
     */
    public static final Comparator<Permission> ID_COMPARATOR = new Comparator<Permission>() {

        /**
         * {@inheritDoc}
         */
        // break eclipse compilation 
        //Override
        public int compare(Permission one, Permission two) {
            return one.getId().compareTo(two.getId());
        }
    };

    //TODO: review and check whether we can do it private
    public final Class owner;

    //TODO: review and check whether we can do it private
    public final PermissionGroup group;

    /**
     * Human readable ID of the permission.
     *
     * <p>
     * This name should uniquely determine a permission among
     * its owner class. The name must be a valid Java identifier.
     * <p>
     * The expected naming convention is something like "BrowseWorkspace".
     */
    //TODO: review and check whether we can do it private
    public final String name;

    /**
     * Human-readable description of this permission.
     * Used as a tooltip to explain this permission, so this message
     * should be a couple of sentences long.
     *
     * <p>
     * If null, there will be no description text.
     */
    //TODO: review and check whether we can do it private
    public final Localizable description;

    /**
     * Bundled {@link Permission} that also implies this permission.
     *
     * <p>
     * This allows us to organize permissions in a hierarchy, so that
     * for example we can say "view workspace" permission is implied by
     * the (broader) "read" permission.
     *
     * <p>
     * The idea here is that for most people, access control based on
     * such broad permission bundle is good enough, and those few
     * that need finer control can do so.
     */
    //TODO: review and check whether we can do it private
    public final Permission impliedBy;

    /**
     * Whether this permission is available for use.
     *
     * <p>
     * This allows us to dynamically enable or disable the visibility of
     * permissions, so administrators can control the complexity of their
     * permission matrix.
     *
     * @since 1.325
     */
    //TODO: review and check whether we can do it private
    public boolean enabled;
    
    /**
     * Defines a new permission.
     *
     * @param group
     *      Permissions are grouped per classes that own them. Specify the permission group
     *      created for that class. The idiom is:
     *
     * <pre>
     * class Foo {
     *     private static final PermissionGroup PERMISSIONS = new PermissionGroup(Foo.class,...);
     *     public static final Permission ABC = new Permisison(PERMISSION,...) ;
     * }
     * </pre>
     *
     *      Because of the classloading problems and the difficulty for Hudson to enumerate them,
     *      the permission constants really need to be static field of the owner class.
     *
     * @param name
     *      See {@link #name}.
     * @param description
     *      See {@link #description}.
     * @param impliedBy
     *      See {@link #impliedBy}.
     */
    public Permission(PermissionGroup group, String name, Localizable description, Permission impliedBy, boolean enable) {
        if(!JSONUtils.isJavaIdentifier(name))
            throw new IllegalArgumentException(name+" is not a Java identifier");
        this.owner = group.owner;
        this.group = group;
        this.name = name;
        this.description = description;
        this.impliedBy = impliedBy;
        this.enabled = enable;

        group.add(this);
        ALL.add(this);
    }

    public Permission(PermissionGroup group, String name, Localizable description, Permission impliedBy) {
        this(group, name, description, impliedBy, true);
    }
    
    /**
     * @deprecated since 1.257.
     *      Use {@link #Permission(PermissionGroup, String, Localizable, Permission)} 
     */
    public Permission(PermissionGroup group, String name, Permission impliedBy) {
        this(group,name,null,impliedBy);
    }

    private Permission(PermissionGroup group, String name) {
        this(group,name,null,null);
    }

    /**
     * Returns the string representation of this {@link Permission},
     * which can be converted back to {@link Permission} via the
     * {@link #fromId(String)} method.
     *
     * <p>
     * This string representation is suitable for persistence.
     *
     * @see #fromId(String)
     */
    public String getId() {
        return owner.getName()+'.'+name;
    }

    public Class getOwner() {
        return owner;
    }

    public PermissionGroup getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public Localizable getDescription() {
        return description;
    }

    public Permission getImpliedBy() {
        return impliedBy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Convert the ID representation into {@link Permission} object.
     *
     * @return
     *      null if the conversion failed.
     * @see #getId()
     */
    public static Permission fromId(String id) {
        int idx = id.lastIndexOf('.');
        if(idx<0)   return null;

        try {
            // force the initialization so that it will put all its permissions into the list.
            Class cl = Class.forName(id.substring(0,idx),true,Hudson.getInstance().getPluginManager().uberClassLoader);
            PermissionGroup g = PermissionGroup.get(cl);
            if(g ==null)  return null;
            return g.find(id.substring(idx+1));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Permission["+owner+','+name+']';
    }

    public void setEnabled(boolean enable) {
        enabled = enable;
    }

    public boolean getEnabled() {
        return enabled;
    }
    
    /**
     * Returns all the {@link Permission}s available in the system.
     * @return
     *      always non-null. Read-only.
     */
    public static List<Permission> getAll() {
        return ALL_VIEW;
    }

    /**
     * All permissions in the system but in a single list.
     */
    private static final List<Permission> ALL = new CopyOnWriteArrayList<Permission>();

    private static final List<Permission> ALL_VIEW = Collections.unmodifiableList(ALL);

//
//
// Because of the initialization order issue, these two fields need to be defined here,
// even though they logically belong to Hudson.
//

    /**
     * {@link PermissionGroup} for {@link Hudson}.
     *
     * @deprecated since 2009-01-23.
     *      Access {@link Hudson#PERMISSIONS} instead.
     */
    public static final PermissionGroup HUDSON_PERMISSIONS = new PermissionGroup(Hudson.class, hudson.model.Messages._Hudson_Permissions_Title());
    /**
     * {@link Permission} that represents the God-like access. Equivalent of Unix root.
     *
     * <p>
     * All permissions are eventually {@linkplain Permission#impliedBy implied by} this permission.
     *
     * @deprecated since 2009-01-23.
     *      Access {@link Hudson#ADMINISTER} instead.
     */
    public static final Permission HUDSON_ADMINISTER = new Permission(HUDSON_PERMISSIONS,"Administer", hudson.model.Messages._Hudson_AdministerPermission_Description(),null);

//
//
// Root Permissions.
//
// These permisisons are meant to be used as the 'impliedBy' permission for other more specific permissions.
// The intention is to allow a simplified AuthorizationStrategy implementation agnostic to
// specific permissions.

    public static final PermissionGroup GROUP = new PermissionGroup(Permission.class,Messages._Permission_Permissions_Title());

    /**
     * Historically this was separate from {@link #HUDSON_ADMINISTER} but such a distinction doesn't make sense
     * any more, so deprecated.
     *
     * @deprecated since 2009-01-23.
     *      Use {@link Hudson#ADMINISTER}.
     */
    public static final Permission FULL_CONTROL = new Permission(GROUP,"FullControl",HUDSON_ADMINISTER);

    /**
     * Generic read access.
     */
    public static final Permission READ = new Permission(GROUP,"GenericRead",null,HUDSON_ADMINISTER);

    /**
     * Generic write access.
     */
    public static final Permission WRITE = new Permission(GROUP,"GenericWrite",null,HUDSON_ADMINISTER);

    /**
     * Generic create access.
     */
    public static final Permission CREATE = new Permission(GROUP,"GenericCreate",null,WRITE);

    /**
     * Generic update access.
     */
    public static final Permission UPDATE = new Permission(GROUP,"GenericUpdate",null,WRITE);

    /**
     * Generic delete access.
     */
    public static final Permission DELETE = new Permission(GROUP,"GenericDelete",null,WRITE);

    /**
     * Generic configuration access.
     */
    public static final Permission CONFIGURE = new Permission(GROUP,"GenericConfigure",null,UPDATE);
}
