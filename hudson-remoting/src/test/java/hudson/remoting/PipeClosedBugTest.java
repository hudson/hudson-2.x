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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

/**
 * Test for "http://issues.hudson-ci.org/browse/HUDSON-7809"
 * Test for "http://issues.hudson-ci.org/browse/HUDSON-8592"
 *
 * @author Winston Prakash
 */
public class PipeClosedBugTest extends RmiTestBase implements Serializable {

    /**
      * Have the reader close the read end of the pipe while the writer is still writing.
      * The writer should pick up a failure.
      */
     public void testReaderCloseWhileWriterIsStillWriting() throws Exception {
         final Pipe p = Pipe.createRemoteToLocal();
         final Future<Void> f = channel.callAsync(new InfiniteWriter(p));
         final InputStream in = p.getIn();
         assertEquals(in.read(), 0);
         in.close();

         try {
             f.get();
             fail();
         } catch (ExecutionException e) {
             // should have resulted in an IOException
             if (!(e.getCause() instanceof IOException)) {
                 e.printStackTrace();
                 fail();
             }
         }
     }

     /**
      * Just writes forever to the pipe
      */
     private static class InfiniteWriter implements Callable<Void, Exception> {
         private final Pipe pipe;

         public InfiniteWriter(Pipe pipe) {
             this.pipe = pipe;
         }

         public Void call() throws Exception {
             while (true) {
                 pipe.getOut().write(0);
                 Thread.sleep(10);
             }
         }
     }

}
