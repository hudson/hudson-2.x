/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Nikita Levyankov
 *      
 *
 *******************************************************************************/ 

package hudson.scm;

import hudson.model.User;
import java.util.Collection;

/**
 * Interface that represents entry from change log.
 * Note: this interface is gonna be used in email-ext plugin and some other plugins.
 * So, changing methods signatures could broke existing logic.
 * <p/>
 * <p/>
 * Date: 5/23/11
 *
 * @author Nikita Levyankov
 */
interface ChangeLogEntry {
    ChangeLogSet getParent();

    /**
     * Gets the text fully marked up by {@link ChangeLogAnnotator}.
     *
     * @return annotated message.
     */
    String getMsgAnnotated();

    /**
     * Gets the "commit message".
     * <p/>
     * The exact definition depends on the individual SCM implementation.
     *
     * @return Can be empty but never null.
     */
    String getMsg();

    /**
     * Returns a set of paths in the workspace that was
     * affected by this change.
     * <p/>
     * Contains string like 'foo/bar/zot'. No leading/trailing '/',
     * and separator must be normalized to '/'.
     *
     * @return never null.
     */
    Collection<String> getAffectedPaths();

    /**
     * The user who made this change.
     *
     * @return never null.
     */
    User getAuthor();

    /**
     * Return string representation of user.
     *
     * @return name or id.
     */
    String getUser();

    /**
     * Returns revision version.
     * Some VCS's use string representation of revision number, for ex. git or cvs;
     * perforce, svn - use numeric values for revisions
     *
     * @return revision version.
     */
    String getCurrentRevision();

    /**
     * Returns a set of paths in the workspace that was
     * affected by this change.
     * <p/>
     * Noted: since this is a new interface, some of the SCMs may not have
     * implemented this interface. The default implementation for this
     * interface is throw UnsupportedOperationException
     *
     * @return AffectedFile never null.
     * @since 2.0.1
     */
    Collection<? extends ChangeLogSet.AffectedFile> getAffectedFiles();
}
