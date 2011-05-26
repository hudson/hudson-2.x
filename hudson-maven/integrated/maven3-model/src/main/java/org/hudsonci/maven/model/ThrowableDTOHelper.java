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

package org.hudsonci.maven.model;

import java.util.List;

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
