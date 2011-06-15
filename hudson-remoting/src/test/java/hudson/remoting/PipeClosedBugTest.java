/*
 * The MIT License
 * 
 * Copyright (c) 2011-, Winston Prakash, Oracle Corporation
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
package hudson.remoting;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Test for "http://issues.hudson-ci.org/browse/HUDSON-7809"
 * 
 * @author Winston Prakash
 */
 //TODO remove if we will leave colution based on NotifyDeadWriter command.
public class PipeClosedBugTest extends RmiTestBase implements Serializable {

    public void ignore_testRemoteWrite() throws Exception {
        final Pipe pipe = Pipe.createRemoteToLocal();
        Future<Integer> f = channel.callAsync(new WritingCallable(pipe));

        readFromPipe(pipe);

        int r = f.get();
        assertEquals(5, r);
    }

    public void testFake() throws Exception {
        assertTrue(true);
    }

    private void readFromPipe(Pipe p) throws IOException {
        System.out.println("South: Reading only one byte from pipe and closing stream.");
        InputStream in = p.getIn();
        // Read only one byte from pipe and close, even through the writer continues
        // to write more bytes in to the pipe.
        in.read();
        in.close();
    }

    private static class WritingCallable implements Callable<Integer, IOException> {

        private final Pipe pipe;

        public WritingCallable(Pipe pipe) {
            this.pipe = pipe;
        }

        public Integer call() throws IOException {
            writeToPipe(pipe);
            return 5;
        }

        private void writeToPipe(Pipe pipe) {
            System.out.println("North: Writing several bytes of data to pipe.");
            try {
                OutputStream os = pipe.getOut();
                byte[] buf = new byte[384];
                for (int i = 0; i < 256; i++) {
                    Arrays.fill(buf, (byte) i);
                    os.write(buf, 0, 256);
                }
                os.close();
            } catch (IOException ex) {
                fail("No IOException (Pipe is already closed) Expected.");
            }
        }
    }
}
