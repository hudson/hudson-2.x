/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.utils.tasks;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStep;
import org.apache.commons.lang.time.StopWatch;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link hudson.tasks.BuildStep#perform} operation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class PerformOperation<T extends BuildStep>
    extends OperationSupport<T>
{
    protected final Launcher launcher;

    public PerformOperation(final T owner, final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
        super(owner, build, listener);
        this.launcher = checkNotNull(launcher);
    }

    public boolean execute() throws InterruptedException, IOException {
        StopWatch watch = new StopWatch();
        watch.start();
        log.debug("Executing");
        try {
            boolean result = doExecute();
            log.debug("Finished in {}", watch);
            return result;
        }
        catch (InterruptedException e) {
            log.debug("Failed after {}", watch);
            onFailure(e);
            throw e;
        }
        catch (IOException e) {
            log.debug("Failed after {}", watch);
            onFailure(e);
            throw e;
        }
        catch (Exception e) {
            log.debug("Failed after {}", watch);
            onFailure(e);
            throw new OperationFailure(e);
        }
    }
}
