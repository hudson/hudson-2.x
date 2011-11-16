/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Nikita Levyankov
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

import com.google.common.collect.Iterators;
import hudson.MarkupText;
import hudson.Util;
import hudson.model.AbstractBuild;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents SCM change list.
 * <p/>
 * <p/>
 * Use the "index" view of this object to render the changeset detail page,
 * and use the "digest" view of this object to render the summary page.
 * For the change list at project level, see {@link SCM}.
 * <p/>
 * <p/>
 * {@link Iterator} is expected to return recent changes first.
 *
 * @author Kohsuke Kawaguchi
 * @author Nikia Levyankov
 */
@ExportedBean(defaultVisibility = 999)
public abstract class ChangeLogSet<T extends ChangeLogSet.Entry> implements Iterable<T> {

    /**
     * {@link AbstractBuild} whose change log this object represents.
     */
    //TODO: review and check whether we can do it private
    public final AbstractBuild<?, ?> build;

    protected ChangeLogSet(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    /**
     * Returns true if there's no change.
     *
     * @return if {@link #getLogs()}  returns empty or null list
     */
    public boolean isEmptySet() {
        return CollectionUtils.isEmpty(getLogs());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return isEmptySet()? Iterators.<T>emptyIterator(): getLogs().iterator();
    }

    /**
     * All changes in this change set.
     */
    // method for the remote API.
    @Exported
    public final Object[] getItems() {
        List<T> r = new ArrayList<T>();
        for (T t : this)
            r.add(t);
        return r.toArray();
    }

    /**
     * Optional identification of the kind of SCM being used.
     * @return a short token, such as the SCM's main CLI executable name
     * @since 1.284
     */
    @Exported
    public String getKind() {
        return null;
    }

    /**
     * Returns change log entries.
     *
     * @return logs.
     */
    public abstract Collection<T> getLogs();

    /**
     * Constant instance that represents no changes.
     */
    public static ChangeLogSet<? extends ChangeLogSet.Entry> createEmpty(AbstractBuild build) {
        return new EmptyChangeLogSet(build);
    }

    @ExportedBean(defaultVisibility = 999)
    public static abstract class Entry implements ChangeLogEntry {
        private ChangeLogSet parent;

        public ChangeLogSet getParent() {
            return parent;
        }

        /**
         * Should be invoked before a {@link ChangeLogSet} is exposed to public.
         */
        protected void setParent(ChangeLogSet parent) {
            this.parent = parent;
        }

        /**
         * Returns a set of paths in the workspace that was
         * affected by this change.
         * <p>
         * Noted: since this is a new interface, some of the SCMs may not have
         * implemented this interface. The default implementation for this
         * interface is throw UnsupportedOperationException
         * <p>
         * It doesn't throw NoSuchMethodException because I rather to throw a
         * runtime exception
         *
         * @return AffectedFile never null.
         * @since 1.309
         */
        public Collection<? extends AffectedFile> getAffectedFiles() {
            String scm = getScmKind();
	        throw new UnsupportedOperationException("getAffectedFiles() is not implemented by " + scm);
        }

        /**
         * Gets the text fully marked up by {@link ChangeLogAnnotator}.
         */
        public String getMsgAnnotated() {
            MarkupText markup = new MarkupText(getMsg());
            for (ChangeLogAnnotator a : ChangeLogAnnotator.all())
                a.annotate(parent.build,this,markup);

            return markup.toString(false);
        }

        /**
         * Message escaped for HTML
         */
        public String getMsgEscaped() {
            return Util.escape(getMsg());
        }

        /**
         * {@inheritDoc}
         */
        public String getCurrentRevision() {
            String scm = getScmKind();
            throw new UnsupportedOperationException("getCurrentRevision() is not implemented by " + scm);
        }

        /**
         * Returns scm name.
         * Help method used for throwing exception while executing unimplemented method.
         *
         * @return name.
         */
        private String getScmKind() {
            String scm = "this SCM";
            ChangeLogSet parent = getParent();
            if (null != parent) {
                String kind = parent.getKind();
                if (null != kind && kind.trim().length() > 0) {
                    scm = kind;
                }
            }
            return scm;
        }
    }

    /**
     * Represents a file change. Contains filename, edit type, etc.
     *
     * I checked the API names against some some major SCMs and most SCMs
     * can adapt to this interface with very little changes
     *
     * @see ChangeLogSet.Entry#getAffectedFiles()
     */
    public interface AffectedFile {
        /**
         * The path in the workspace that was affected
         * <p>
         * Contains string like 'foo/bar/zot'. No leading/trailing '/',
         * and separator must be normalized to '/'.
         *
         * @return never null.
         */
        String getPath();

        /**
         * Return whether the file is new/modified/deleted
         */
        EditType getEditType();
    }
}
