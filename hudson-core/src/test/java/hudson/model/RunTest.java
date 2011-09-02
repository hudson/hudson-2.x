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
*    Kohsuke Kawaguchi, Jorg Heymans
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import junit.framework.TestCase;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class RunTest extends TestCase {
    private List<? extends Run<?,?>.Artifact> createArtifactList(String... paths) {
        Run<FreeStyleProject,FreeStyleBuild> r = new Run<FreeStyleProject,FreeStyleBuild>(null,new GregorianCalendar()) {};
        Run<FreeStyleProject,FreeStyleBuild>.ArtifactList list = r.new ArtifactList();
        for (String p : paths) {
            list.add(r.new Artifact(p,p,p,"n"+list.size()));  // Assuming all test inputs don't need urlencoding
        }
        list.computeDisplayName();
        return list;
    }

    public void testArtifactListDisambiguation1() {
        List<? extends Run<?, ?>.Artifact> a = createArtifactList("a/b/c.xml", "d/f/g.xml", "h/i/j.xml");
        assertEquals(a.get(0).getDisplayPath(),"c.xml");
        assertEquals(a.get(1).getDisplayPath(),"g.xml");
        assertEquals(a.get(2).getDisplayPath(),"j.xml");
    }

    public void testArtifactListDisambiguation2() {
        List<? extends Run<?, ?>.Artifact> a = createArtifactList("a/b/c.xml", "d/f/g.xml", "h/i/g.xml");
        assertEquals(a.get(0).getDisplayPath(),"c.xml");
        assertEquals(a.get(1).getDisplayPath(),"f/g.xml");
        assertEquals(a.get(2).getDisplayPath(),"i/g.xml");
    }

    public void testArtifactListDisambiguation3() {
        List<? extends Run<?, ?>.Artifact> a = createArtifactList("a.xml","a/a.xml");
        assertEquals(a.get(0).getDisplayPath(),"a.xml");
        assertEquals(a.get(1).getDisplayPath(),"a/a.xml");
    }

    public void testRunCompare() throws IOException {
        Calendar cal1 = new GregorianCalendar();
        cal1.setTimeInMillis(2);
        Calendar cal2 = new GregorianCalendar();
        cal2.setTimeInMillis(1);
        Calendar cal3 = new GregorianCalendar();
        cal3.setTimeInMillis(3);

        Run<FreeStyleProject,FreeStyleBuild> run1 = new Run<FreeStyleProject, FreeStyleBuild>(null, cal1) {};
        Run<FreeStyleProject,FreeStyleBuild> run2 = new Run<FreeStyleProject, FreeStyleBuild>(null, cal2) {};
        Run<FreeStyleProject,FreeStyleBuild> run3 = new Run<FreeStyleProject, FreeStyleBuild>(null, cal3) {};

        RunMap runMap = new RunMap();
        runMap.put(1, run1);
        runMap.put(2, run2);
        runMap.put(3, run3);
        runMap.remove(run2);
    }

}
