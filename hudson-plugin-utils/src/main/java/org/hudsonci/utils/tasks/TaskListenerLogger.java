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

import hudson.model.TaskListener;
import org.sonatype.gossip.Level;
import org.sonatype.gossip.render.PatternRenderer;
import org.sonatype.gossip.render.Renderer;
import org.sonatype.gossip.support.PrintStreamLogger;

/**
 * Adapts a {@link TaskListener} to the {@link org.slf4j.Logger} interface.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class TaskListenerLogger
    extends PrintStreamLogger
{
    public TaskListenerLogger(final TaskListener listener, final Level level) {
        super(listener.getLogger(), level);
    }

    public TaskListenerLogger(final TaskListener listener) {
        super(listener.getLogger());
    }

    @Override
    protected Renderer createRenderer() {
        return new PatternRenderer("[%l] %m%n%x"); // ignore the logger name
    }
}
