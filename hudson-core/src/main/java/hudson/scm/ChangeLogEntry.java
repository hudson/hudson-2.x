/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
