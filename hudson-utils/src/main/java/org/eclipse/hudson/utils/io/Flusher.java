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

package org.eclipse.hudson.utils.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Flushable;
import java.io.IOException;

/**
 * Quietly flushes {@link Flushable} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class Flusher
{
    private static final Logger log = LoggerFactory.getLogger(Flusher.class);

    /**
     * @param targets objects to flush, null is permitted
     */
    public static void flush(final Flushable... targets) {
        if (targets != null) {
            for (Flushable f : targets) {
                if (f == null) {
                    continue;
                }

                try {
                    f.flush();
                }
                catch (IOException e) {
                    log.trace(e.getMessage(), e);
                }
            }
        }
    }
}
