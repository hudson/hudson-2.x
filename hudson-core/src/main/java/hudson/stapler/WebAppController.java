/*
 * The MIT License
 *
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
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
package hudson.stapler;

import javax.servlet.ServletContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles management of the Stapler root "app" object.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class WebAppController {

    private static final Logger log = Logger.getLogger(WebAppController.class.getName());
    private static final String APP = "app";
    private InstallStrategy installStrategy;
    private ServletContext context;

    // Sync everything, this class should not be used often, and needs to ensure consistent state
    public synchronized ServletContext getContext() {
        if (context == null) {
            throw new IllegalStateException();
        }
        return context;
    }

    public synchronized void setContext(final ServletContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (this.context != null) {
            throw new IllegalStateException();
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Context initialized: " + context);
        }
        this.context = context;
    }

    public synchronized InstallStrategy getInstallStrategy() {
        return installStrategy;
    }

    public synchronized void setInstallStrategy(final InstallStrategy strategy) {
        if (strategy == null) {
            throw new NullPointerException();
        }
        if (this.installStrategy != null) {
            throw new IllegalStateException();
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Strategy initialized: " + strategy);
        }
        this.installStrategy = strategy;
    }

    public synchronized void install(final Object app) {
        if (app == null) {
            throw new NullPointerException();
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Attempting to install app: " + app);
        }
        if (getInstallStrategy().isAllowed(app)) {
            getContext().setAttribute(APP, app);
        } else {
            log.warning("Strategy denied install");
        }
    }

    public synchronized Object current() {
        return getContext().getAttribute(APP);
    }

    /**
     * Strategy to control which object is installed.
     */
    public static interface InstallStrategy {

        boolean isAllowed(Object app);
    }

    /**
     * Always install the given object.
     */
    public static class DefaultInstallStrategy
            implements InstallStrategy {

        public boolean isAllowed(final Object app) {
            return true;
        }
    }
    //
    // Instance access (since there is no DI setup at this point).
    //
    private static final WebAppController INSTANCE = new WebAppController();

    public static WebAppController get() {
        return INSTANCE;
    }
}
