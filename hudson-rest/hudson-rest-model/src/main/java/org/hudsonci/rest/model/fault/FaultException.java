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

package org.hudsonci.rest.model.fault;

import java.util.Iterator;

/**
 * {@link FaultDTO} exception container.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class FaultException
    extends RuntimeException
{
    // This is pointless, since Fault does not impl Serializable
    private static final long serialVersionUID = 1L;

    private final FaultDTO fault;

    public FaultException(final FaultDTO fault) {
        assert fault != null;
        this.fault = fault;
    }

    public FaultException(final String type, final String message) {
        this(FaultBuilder.build(type, message));
    }

    public FaultDTO getFault() {
        return fault;
    }

    @Override
    public String getMessage() {
        StringBuffer buff = new StringBuffer();
        buff.append("Fault: ").append(fault.getId()).append("\n");

        Iterator<FaultDetailDTO> iter = fault.getDetails().iterator();
        while (iter.hasNext()) {
            FaultDetailDTO detail = iter.next();
            buff.append(String.format("[%s] %s", detail.getType(), detail.getMessage()));
            if (iter.hasNext()) {
                buff.append(", ");
            }
        }

        return buff.toString();
    }
}
