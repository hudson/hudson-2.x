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
