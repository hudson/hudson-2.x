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

package org.eclipse.hudson.maven.model;

import java.util.List;

import org.eclipse.hudson.maven.model.StackTraceDTO;
import org.eclipse.hudson.maven.model.ThrowableDTO;

/**
 * Helper for {@link ThrowableDTO}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ThrowableDTOHelper
{
    public static ThrowableDTO convert(final Throwable source, final boolean deep) {
        assert source != null;

        ThrowableDTO target = new ThrowableDTO()
                .withType(source.getClass().getName())
                .withMessage(source.getMessage())
                .withStackTrace(convert(source.getStackTrace()));

        if (deep) {
            Throwable cause = source.getCause();
            if (cause != null) {
                target.withCause(convert(cause, deep));
            }
        }

        return target;
    }

    public static ThrowableDTO convert(final Throwable source) {
        return convert(source, true);
    }

    public static StackTraceDTO convert(final StackTraceElement[] source) {
        assert source != null;

        StackTraceDTO target = new StackTraceDTO();
        List<StackTraceDTO.Element> elements = target.getElements();
        for (StackTraceElement element : source) {
            elements.add(convert(element));
        }

        return target;
    }

    public static StackTraceDTO.Element convert(final StackTraceElement source) {
        assert source != null;

        StackTraceDTO.Element target = new StackTraceDTO.Element()
                .withType(source.getClassName())
                .withMethod(source.getMethodName())
                .withFile(source.getFileName())
                .withLine(source.getLineNumber());

        return target;
    }
}
