/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.model.queue;

import hudson.model.Queue.Task;
import hudson.model.Node;
import hudson.model.Messages;
import hudson.model.Label;
import org.jvnet.localizer.Localizable;

/**
 * If a {@link Task} execution is blocked in the queue, this object represents why.
 *
 * <h2>View</h2>
 * <tt>summary.jelly</tt> should do one-line HTML rendering to be used while rendering
 * "build history" widget, next to the blocking build. By default it simply renders
 * {@link #getShortDescription()} text.
 *
 * @since 1.330
 */
public abstract class CauseOfBlockage {
    /**
     * Human readable description of why the build is blocked.
     */
    public abstract String getShortDescription();

    /**
     * Obtains a simple implementation backed by {@link Localizable}.
     */
    public static CauseOfBlockage fromMessage(final Localizable l) {
        return new CauseOfBlockage() {
            public String getShortDescription() {
                return l.toString();
            }
        };
    }

    /**
     * Build is blocked because a node is offline.
     */
    public static final class BecauseNodeIsOffline extends CauseOfBlockage {
        //TODO: review and check whether we can do it private
        public final Node node;

        public Node getNode() {
            return node;
        }

        public BecauseNodeIsOffline(Node node) {
            this.node = node;
        }

        public String getShortDescription() {
            return Messages.Queue_NodeOffline(node.getDisplayName());
        }
    }

    /**
     * Build is blocked because all the nodes that match a given label is offline.
     */
    public static final class BecauseLabelIsOffline extends CauseOfBlockage {
        //TODO: review and check whether we can do it private
        public final Label label;

        public Label getLabel() {
            return label;
        }

        public BecauseLabelIsOffline(Label l) {
            this.label = l;
        }

        public String getShortDescription() {
            return Messages.Queue_AllNodesOffline(label.getName());
        }
    }

    /**
     * Build is blocked because a node is fully busy
     */
    public static final class BecauseNodeIsBusy extends CauseOfBlockage {
        //TODO: review and check whether we can do it private
        public final Node node;

        public Node getNode() {
            return node;
        }

        public BecauseNodeIsBusy(Node node) {
            this.node = node;
        }

        public String getShortDescription() {
            return Messages.Queue_WaitingForNextAvailableExecutorOn(node.getNodeName());
        }
    }

    /**
     * Build is blocked because everyone that matches the specified label is fully busy
     */
    public static final class BecauseLabelIsBusy extends CauseOfBlockage {
        //TODO: review and check whether we can do it private
        public final Label label;

        public Label getLabel() {
            return label;
        }

        public BecauseLabelIsBusy(Label label) {
            this.label = label;
        }

        public String getShortDescription() {
            return Messages.Queue_WaitingForNextAvailableExecutorOn(label.getName());
        }
    }
}
