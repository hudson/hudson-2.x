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

package org.hudsonci.maven.plugin.builder.internal.invoker;

import org.hudsonci.utils.io.Closer;
import org.hudsonci.maven.model.InvocationDTO;
import com.thoughtworks.xstream.XStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handler which records invocations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class RecordingHandler
    extends DelegatingInvocationHandler
{
    public static final String INVOCATION_STREAM = "invocation-stream";

    private final XStream xs;

    private final File file;

    private final Writer writer;

    private final ObjectOutputStream output;

    public RecordingHandler(final InvocationHandler delegate, final File file) throws IOException {
        super(delegate);

        assert file != null;
        this.file = file;
        log.debug("Recording invocations to: {}", file);

        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        xs = new XStream();
        xs.autodetectAnnotations(true);
        writer = new BufferedWriter(new FileWriter(file));
        output = xs.createObjectOutputStream(writer, INVOCATION_STREAM);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        assert method != null;

        InvocationDTO.Result result = new InvocationDTO.Result();
        InvocationDTO invocation = invocationOf(method, args).withResult(result);

        Object value;
        try {
            value = getDelegate().invoke(proxy, method, args);
            result.withException(false).withValue(value);
        }
        catch (Throwable t) {
            result.withException(true).withValue(t);
            throw t;
        }
        finally {
            try {
                synchronized (output) {
                    output.writeObject(invocation);
                }
            }
            catch (IOException e) {
                log.error("Write object failed", e);
            }
        }

        return value;
    }

    private InvocationDTO invocationOf(final Method method, final Object[] args) {
        assert method != null;
        // args may be null

        InvocationDTO.Method imethod = new InvocationDTO.Method().withName(method.getName());
        for (Class type : method.getParameterTypes()) {
            imethod.getTypes().add(type.getName());
        }

        return new InvocationDTO().withMethod(imethod).withArgs(args);
    }

    // TODO: Add a class to read a recorded session and translate into invokes on a Callback instance

    public void close() {
        Closer.close(output);
    }
}
