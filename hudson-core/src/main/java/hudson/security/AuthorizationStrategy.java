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
*    Kohsuke Kawaguchi, Seiji Sogabe, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.security;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.*;
import hudson.slaves.Cloud;
import hudson.util.DescriptorList;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import net.sf.json.JSONObject;

import org.acegisecurity.Authentication;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Controls authorization throughout Hudson.
 *
 * <h2>Persistence</h2>
 * <p>
 * This object will be persisted along with {@link Hudson} object.
 * Hudson by itself won't put the ACL returned from {@link #getRootACL()} into the serialized object graph,
 * so if that object contains state and needs to be persisted, it's the responsibility of
 * {@link AuthorizationStrategy} to do so (by keeping them in an instance field.)
 *
 * <h2>Re-configuration</h2>
 * <p>
 * The corresponding {@link Describable} instance will be asked to create a new {@link AuthorizationStrategy}
 * every time the system configuration is updated. Implementations that keep more state in ACL beyond
 * the system configuration should use {@link Hudson#getAuthorizationStrategy()} to talk to the current
 * instance to carry over the state. 
 *
 * @author Kohsuke Kawaguchi
 * @see SecurityRealm
 */
public abstract class AuthorizationStrategy extends AbstractDescribableImpl<AuthorizationStrategy> implements ExtensionPoint {
    /**
     * Returns the instance of {@link ACL} where all the other {@link ACL} instances
     * for all the other model objects eventually delegate.
     * <p>
     * IOW, this ACL will have the ultimate say on the access control.
     */
    public abstract ACL getRootACL();

    /**
     * @deprecated since 1.277
     *      Override {@link #getACL(Job)} instead.
     */
    @Deprecated
    public ACL getACL(AbstractProject<?,?> project) {
    	return getACL((Job)project);
    }

    public ACL getACL(Job<?,?> project) {
    	return getRootACL();
    }

    /**
     * Implementation can choose to provide different ACL for different views.
     * This can be used as a basis for more fine-grained access control.
     *
     * <p>
     * The default implementation returns the ACL of the ViewGroup.
     *
     * @since 1.220
     */
    public ACL getACL(View item) {
    	return item.getOwner().getACL();
    }
    
    /**
     * Implementation can choose to provide different ACL for different items.
     * This can be used as a basis for more fine-grained access control.
     *
     * <p>
     * The default implementation returns {@link #getRootACL()}.
     *
     * @since 1.220
     */
    public ACL getACL(AbstractItem item) {
        return getRootACL();
    }

    /**
     * Implementation can choose to provide different ACL per user.
     * This can be used as a basis for more fine-grained access control.
     *
     * <p>
     * The default implementation returns {@link #getRootACL()}.
     *
     * @since 1.221
     */
    public ACL getACL(User user) {
        return getRootACL();
    }

    /**
     * Implementation can choose to provide different ACL for different computers.
     * This can be used as a basis for more fine-grained access control.
     *
     * <p>
     * The default implementation delegates to {@link #getACL(Node)}
     *
     * @since 1.220
     */
    public ACL getACL(Computer computer) {
        return getACL(computer.getNode());
    }

    /**
     * Implementation can choose to provide different ACL for different {@link Cloud}s.
     * This can be used as a basis for more fine-grained access control.
     *
     * <p>
     * The default implementation returns {@link #getRootACL()}.
     *
     * @since 1.252
     */
    public ACL getACL(Cloud cloud) {
        return getRootACL();
    }

    public ACL getACL(Node node) {
        return getRootACL();
    }

    /**
     * Returns the list of all group/role names used in this authorization strategy,
     * and the ACL returned from the {@link #getRootACL()} method.
     * <p>
     * This method is used by {@link ContainerAuthentication} to work around the servlet API issue
     * that prevents us from enumerating roles that the user has.
     * <p>
     * If such enumeration is impossible, do the best to list as many as possible, then
     * return it. In the worst case, just return an empty list. Doing so would prevent
     * users from using role names as group names (see HUDSON-2716 for such one such report.)
     *
     * @return
     *      never null.
     */
    public abstract Collection<String> getGroups();

    /**
     * Returns all the registered {@link AuthorizationStrategy} descriptors.
     */
    public static DescriptorExtensionList<AuthorizationStrategy,Descriptor<AuthorizationStrategy>> all() {
        return Hudson.getInstance().<AuthorizationStrategy,Descriptor<AuthorizationStrategy>>getDescriptorList(AuthorizationStrategy.class);
    }

    /**
     * All registered {@link SecurityRealm} implementations.
     *
     * @deprecated since 1.286
     *      Use {@link #all()} for read access, and {@link Extension} for registration.
     */
    public static final DescriptorList<AuthorizationStrategy> LIST = new DescriptorList<AuthorizationStrategy>(AuthorizationStrategy.class);
    
    /**
     * {@link AuthorizationStrategy} that implements the semantics
     * of unsecured Hudson where everyone has full control.
     *
     * <p>
     * This singleton is safe because {@link Unsecured} is stateless.
     */
    public static final AuthorizationStrategy UNSECURED = new Unsecured();

    public static final class Unsecured extends AuthorizationStrategy implements Serializable {
        /**
         * Maintains the singleton semantics.
         */
        private Object readResolve() {
            return UNSECURED;
        }

        @Override
        public ACL getRootACL() {
            return UNSECURED_ACL;
        }

        public Collection<String> getGroups() {
            return Collections.emptySet();
        }

        private static final ACL UNSECURED_ACL = new ACL() {
            public boolean hasPermission(Authentication a, Permission permission) {
                return true;
            }
        };

        @Extension
        public static final class DescriptorImpl extends Descriptor<AuthorizationStrategy> {
            public String getDisplayName() {
                return Messages.AuthorizationStrategy_DisplayName();
            }

            @Override
            public AuthorizationStrategy newInstance(StaplerRequest req, JSONObject formData) throws FormException {
                return UNSECURED;
            }

            @Override
            public String getHelpFile() {
                return "/help/security/no-authorization.html";
            }
        }
    }
}
