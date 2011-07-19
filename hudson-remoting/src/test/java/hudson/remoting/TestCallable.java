/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.remoting;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/**
 * {@link Callable} used to verify the classloader used.
 *
 * @author Kohsuke Kawaguchi
 */
public class TestCallable implements Callable {
    public Object call() throws Throwable {
        Object[] r = new Object[4];

        // to verify that this class is indeed loaded by the remote classloader
        r[0] = getClass().getClassLoader().toString();

        // to make sure that we can also load resources
        String resName = "TestCallable.class";
        InputStream in = getClass().getResourceAsStream(resName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[8192];
        int len;
        while((len=in.read(buf))>0)
            baos.write(buf,0,len);
        in.close();

        r[1] = baos.toByteArray();

        // to make sure multiple resource look ups are cached.
        r[2] = getClass().getResource(resName);
        r[3] = getClass().getResource(resName);

        return r;
    }

}
