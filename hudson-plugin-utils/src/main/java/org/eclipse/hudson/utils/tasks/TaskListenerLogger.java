/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.utils.tasks;

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
