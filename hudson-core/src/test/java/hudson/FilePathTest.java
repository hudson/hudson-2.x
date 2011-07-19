/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi, Alan Harder
 *     
 *
 *******************************************************************************/ 

package hudson;

import hudson.remoting.VirtualChannel;
import hudson.util.NullStream;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.output.NullOutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public class FilePathTest extends ChannelTestCase {

    public void testCopyTo() throws Exception {
        File tmp = File.createTempFile("testCopyTo","");
        FilePath f = new FilePath(french,tmp.getPath());
        f.copyTo(new NullStream());
        assertTrue("target does not exist", tmp.exists());
        assertTrue("could not delete target " + tmp.getPath(), tmp.delete());
    }

    /**
     * An attempt to reproduce the file descriptor leak.
     * If this operation leaks a file descriptor, 2500 should be enough, I think.
     */
    public void testCopyTo2() throws Exception {
        for (int j=0; j<2500; j++) {
            File tmp = File.createTempFile("testCopyFrom","");
            FilePath f = new FilePath(tmp);
            File tmp2 = File.createTempFile("testCopyTo","");
            FilePath f2 = new FilePath(british,tmp2.getPath());

            f.copyTo(f2);

            f.delete();
            f2.delete();
        }
    }

    public void testRepeatCopyRecursiveTo() throws Exception {
        // local->local copy used to return 0 if all files were "up to date"
        // should return number of files processed, whether or not they were copied or already current
        File tmp = Util.createTempDir(), src = new File(tmp, "src"), dst = new File(tmp, "dst");
        try {
            assertTrue(src.mkdir());
            assertTrue(dst.mkdir());
            File f = File.createTempFile("foo", ".tmp", src);
            FilePath fp = new FilePath(src);
            assertEquals(1, fp.copyRecursiveTo(new FilePath(dst)));
            // copy again should still report 1
            assertEquals(1, fp.copyRecursiveTo(new FilePath(dst)));
        } finally {
            Util.deleteRecursive(tmp);
        }

    }

    /**
     * Attempt to reproduce  http://issues.hudson-ci.org/browse/HUDSON-2154
     */
    public void testRemoteFileCopyRecursiveTo() throws IOException, InterruptedException {
        File tmp = Util.createTempDir(), src = new File(tmp, "src"), dst = new File(tmp, "dst");
        try {

            assertTrue(src.mkdir());
            assertTrue(dst.mkdir());

            FilePath localFilePath = new FilePath(french, dst.getPath());

            FilePath remoteFilePath = new FilePath(british, src.getPath());
            remoteFilePath.unzipFrom(getClass().getResourceAsStream("/hudson/remoteCopyFiles.zip"));
            remoteFilePath.copyRecursiveTo("**/*", localFilePath);
            for (FilePath child : localFilePath.list()) {
                System.out.println(child.getName());
                if (child.isDirectory()) {
                    for (FilePath child2 : child.list()) {
                        System.out.println(child2.getName());
                    }
                }
            }
        } finally {
            Util.deleteRecursive(tmp);
        }
    }

    public void testArchiveBug4039() throws Exception {
        File tmp = Util.createTempDir();
        try {
            FilePath d = new FilePath(french,tmp.getPath());
            d.child("test").touch(0);
            d.zip(new NullOutputStream());
            d.zip(new NullOutputStream(),"**/*");
        } finally {
            Util.deleteRecursive(tmp);
        }
    }

    public void testNormalization() throws Exception {
        compare("abc/def\\ghi","abc/def\\ghi"); // allow mixed separators

        {// basic '.' trimming
            compare("./abc/def","abc/def");
            compare("abc/./def","abc/def");
            compare("abc/def/.","abc/def");

            compare(".\\abc\\def","abc\\def");
            compare("abc\\.\\def","abc\\def");
            compare("abc\\def\\.","abc\\def");
        }

        compare("abc/../def","def");
        compare("abc/def/../../ghi","ghi");
        compare("abc/./def/../././../ghi","ghi");   // interleaving . and ..

        compare("../abc/def","../abc/def");     // uncollapsible ..
        compare("abc/def/..","abc");

        compare("c:\\abc\\..","c:\\");      // we want c:\\, not c:
        compare("c:\\abc\\def\\..","c:\\abc");

        compare("/abc/../","/");
        compare("abc/..",".");
        compare(".",".");

        // @Bug(5951)
        compare("C:\\Hudson\\jobs\\foo\\workspace/../../otherjob/workspace/build.xml",
                "C:\\Hudson\\jobs/otherjob/workspace/build.xml");
        // Other cases that failed before
        compare("../../abc/def","../../abc/def");
        compare("..\\..\\abc\\def","..\\..\\abc\\def");
        compare("/abc//../def","/def");
        compare("c:\\abc\\\\..\\def","c:\\def");
        compare("/../abc/def","/abc/def");
        compare("c:\\..\\abc\\def","c:\\abc\\def");
        compare("abc/def/","abc/def");
        compare("abc\\def\\","abc\\def");
        // The new code can collapse extra separator chars
        compare("abc//def/\\//\\ghi","abc/def/ghi");
        compare("\\\\host\\\\abc\\\\\\def","\\\\host\\abc\\def"); // don't collapse for \\ prefix
        compare("\\\\\\foo","\\\\foo");
        compare("//foo","/foo");
        // Other edge cases
        compare("abc/def/../../../ghi","../ghi");
        compare("\\abc\\def\\..\\..\\..\\ghi\\","\\ghi");
    }

    private void compare(String original, String answer) {
        assertEquals(answer,new FilePath((VirtualChannel)null,original).getRemote());
    }

    // @Bug(6494)
    public void testGetParent() throws Exception {
        FilePath fp = new FilePath((VirtualChannel)null, "/abc/def");
        assertEquals("/abc", (fp = fp.getParent()).getRemote());
        assertEquals("/", (fp = fp.getParent()).getRemote());
        assertNull(fp.getParent());

        fp = new FilePath((VirtualChannel)null, "abc/def\\ghi");
        assertEquals("abc/def", (fp = fp.getParent()).getRemote());
        assertEquals("abc", (fp = fp.getParent()).getRemote());
        assertNull(fp.getParent());

        fp = new FilePath((VirtualChannel)null, "C:\\abc\\def");
        assertEquals("C:\\abc", (fp = fp.getParent()).getRemote());
        assertEquals("C:\\", (fp = fp.getParent()).getRemote());
        assertNull(fp.getParent());
    }
}
