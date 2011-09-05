/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.slaves;

import hudson.model.Computer;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Exported;

/**
 * Represents a cause that puts a {@linkplain Computer#isOffline() computer offline}.
 *
 * <h2>Views</h2>
 * <p>
 * {@link OfflineCause} must have <tt>cause.jelly</tt> that renders a cause
 * into HTML. This is used to tell users why the node is put offline.
 * This view should render a block element like DIV. 
 *
 * @author Kohsuke Kawaguchi
 * @since 1.320
 */
@ExportedBean
public abstract class OfflineCause {
    /**
     * {@link OfflineCause} that renders a static text,
     * but without any further UI.
     */
    public static class SimpleOfflineCause extends OfflineCause {
        //TODO: review and check whether we can do it private
        public final Localizable description;

        public Localizable getDescription() {
            return description;
        }

        private SimpleOfflineCause(Localizable description) {
            this.description = description;
        }

        @Exported(name="description") @Override
        public String toString() {
            return description.toString();
        }
    }

    public static OfflineCause create(Localizable d) {
        if (d==null)    return null;
        return new SimpleOfflineCause(d);
    }

    /**
     * Caused by unexpected channel termination.
     */
    public static class ChannelTermination extends OfflineCause {
        //TODO: review and check whether we can do it private
        @Exported
        public final Exception cause;

        public Exception getCause() {
            return cause;
        }

        public ChannelTermination(Exception cause) {
            this.cause = cause;
        }

        public String getShortDescription() {
            return cause.toString();
        }
    }

    /**
     * Caused by failure to launch.
     */
    public static class LaunchFailed extends OfflineCause {
        @Override
        public String toString() {
            return Messages.OfflineCause_LaunchFailed();
        }
    }

    public static class ByCLI extends OfflineCause {
        //TODO: review and check whether we can do it private
        @Exported
        public final String message;

        public String getMessage() {
            return message;
        }

        public ByCLI(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            if (message==null)
                return Messages.OfflineCause_DisconnectedFromCLI();
            return message;
        }
    }
}
