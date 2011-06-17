/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *      Winston Prakash
 *
 *******************************************************************************/ 

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
public class PipeClosedBugTest extends RmiTestBase implements Serializable {

    public void testRemoteWrite() throws Exception {
        final Pipe pipe = Pipe.createRemoteToLocal();
        Future<Integer> f = channel.callAsync(new WritingCallable(pipe));

        readFromPipe(pipe);

        int r = f.get();
        assertEquals(5, r);
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
