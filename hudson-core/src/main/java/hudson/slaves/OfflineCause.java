/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc.
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
