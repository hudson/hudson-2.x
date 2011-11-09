package hudson.scm;

import java.io.Serializable;

/**
 * Immutable object that represents the result of {@linkplain SCM#poll(hudson.model.AbstractProject, hudson.Launcher, hudson.FilePath, hudson.model.TaskListener, SCMRevisionState) SCM polling}.
 *
 * <p>
 * This object is marked serializable just to be remoting friendly &mdash; Hudson by itself
 * doesn't persist this object.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.345
 */
public final class PollingResult implements Serializable {
    /**
     * Baseline of the comparison.
     * (This comes from either the workspace, or from the remote repository as of the last polling.
     * Can be null.
     */
    //TODO: review and check whether we can do it private
    public final SCMRevisionState baseline;

    /**
     * Current state of the remote repository. To be passed to the next invocation of the polling method.
     * Can be null.
     */
    //TODO: review and check whether we can do it private
    public final SCMRevisionState remote;

    /**
     * Degree of the change between baseline and remote. Never null.
     * <p>
     * The fact that this field is independent from {@link #baseline} and {@link #remote} are
     * used to (1) allow the {@linkplain Change#INCOMPARABLE incomparable} state which forces
     * the immediate rebuild, and (2) allow SCM to ignore some changes in the repository to implement
     * exclusion feature.
     */
    //TODO: review and check whether we can do it private
    public final Change change;

    /**
     * Degree of changes between the previous state and this state.
     */
    public enum Change {
        /**
         * No change. Two {@link SCMRevisionState}s point to the same state of the same repository / the same commit.
         */
        NONE,
        /**
         * There are some changes between states, but those changes are not significant enough to consider
         * a new build. For example, some SCMs allow certain commits to be marked as excluded, and this is how
         * you can do it.
         */
        INSIGNIFICANT,
        /**
         * There are changes between states that warrant a new build. Hudson will eventually
         * schedule a new build for this change, subject to other considerations
         * such as the quiet period.
         */
        SIGNIFICANT,
        /**
         * The state as of baseline is so different from the current state that they are incomparable
         * (for example, the workspace and the remote repository points to two unrelated repositories
         * because the configuration has changed.) This forces Hudson to schedule a build right away.
         * <p>
         * This is primarily useful in SCM implementations that require a workspace to be able
         * to perform a polling. SCMs that can always compare remote revisions regardless of the local
         * state should do so, and never return this constant, to let Hudson maintain the quiet period
         * behavior all the time.
         * <p>
         * This constant is not to be confused with the errors encountered during polling, which
         * should result in an exception and eventual retry by Hudson.
         */
        INCOMPARABLE
    }

    public PollingResult(SCMRevisionState baseline, SCMRevisionState remote, Change change) {
        if (change==null)   throw new IllegalArgumentException();
        this.baseline = baseline;
        this.remote = remote;
        this.change = change;
    }

    public PollingResult(Change change) {
        this(null,null,change);
    }

    public SCMRevisionState getBaseline() {
        return baseline;
    }

    public SCMRevisionState getRemote() {
        return remote;
    }

    public Change getChange() {
        return change;
    }

    public boolean hasChanges() {
        return change.ordinal() > Change.INSIGNIFICANT.ordinal();
    }

    /**
     * Constant to indicate no changes in the remote repository.
     */
    public static final PollingResult NO_CHANGES = new PollingResult(Change.NONE);

    public static final PollingResult SIGNIFICANT = new PollingResult(Change.SIGNIFICANT);

    /**
     * Constant that uses {@link Change#INCOMPARABLE} which forces an immediate build.
     */
    public static final PollingResult BUILD_NOW = new PollingResult(Change.INCOMPARABLE);

    private static final long serialVersionUID = 1L;
}
