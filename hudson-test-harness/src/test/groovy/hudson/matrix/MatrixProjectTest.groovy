/**************************************************************************
#
# Copyright (C) 2004-2009 Oracle Corporation
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#         Kohsuke Kawaguchi
#
#**************************************************************************/ 

package hudson.matrix

import hudson.model.Cause
import hudson.model.Result
import hudson.tasks.Ant
import hudson.tasks.ArtifactArchiver
import hudson.tasks.Fingerprinter
import hudson.tasks.Maven
import hudson.tasks.Shell
import hudson.tasks.BatchFile
import org.jvnet.hudson.test.Email
import org.jvnet.hudson.test.HudsonTestCase
import org.jvnet.hudson.test.SingleFileSCM
import org.jvnet.hudson.test.UnstableBuilder
import com.gargoylesoftware.htmlunit.html.HtmlTable
import org.jvnet.hudson.test.Bug
import org.jvnet.hudson.test.TestBuilder
import hudson.model.AbstractBuild
import hudson.Launcher
import hudson.model.BuildListener
import hudson.util.OneShotEvent
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import hudson.model.JDK
import hudson.model.Slave
import hudson.Functions

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class MatrixProjectTest extends HudsonTestCase {

    void assertRectangleTable(MatrixProject p) {
        def html = createWebClient().getPage(p);
        HtmlTable table = html.selectSingleNode("id('matrix')/table")

        // remember cells that are extended from rows above.
        def rowSpans = [:];
        def masterWidth = null
        for (r in table.rows) {
            int width = r.cells*.columnSpan.sum() + rowSpans.values().sum(0);
            if (masterWidth==null)
                masterWidth = width;
            else
                assertEquals(masterWidth,width);

            for (c in r.cells)
                rowSpans[c.rowSpan] = (rowSpans[c.rowSpan]?:0)+c.columnSpan
            // shift rowSpans by one
            def nrs =[:]
            rowSpans.each { k,v -> if(k>1) nrs[k-1]=v }
            rowSpans = nrs
        }
    }

    @Bug(4245)
    void testLayout1() {
        // 5*5*5*5*5 matrix
        def p = createMatrixProject();
        p.axes = new AxisList(
            ['a','b','c','d','e'].collect { name -> new TextAxis(name, (1..4)*.toString() ) }
        );
        assertRectangleTable(p)
    }

    @Bug(4245)
    void testLayout2() {
        // 2*3*4*5*6 matrix
        def p = createMatrixProject();
        p.axes = new AxisList(
            (2..6).collect { n -> new TextAxis("axis${n}", (1..n)*.toString() ) }
        );
        assertRectangleTable(p)
    }

    /**
     * Makes sure that the configuration correctly roundtrips.
     */
    public void testConfigRoundtrip() {
        hudson.getJDKs().addAll([
                new JDK("jdk1.7","somewhere"),
                new JDK("jdk1.6","here"),
                new JDK("jdk1.5","there")]);

        List<Slave> slaves = (0..2).collect { createSlave() }

        def p = createMatrixProject();
        p.axes.add(new JDKAxis(["jdk1.6","jdk1.5"]));
        p.axes.add(new LabelAxis("label1",[slaves[0].nodeName, slaves[1].nodeName]));
        p.axes.add(new LabelAxis("label2",[slaves[2].nodeName])); // make sure single value handling works OK
        def o = new AxisList(p.axes);
        configRoundtrip(p);
        def n = p.axes;

        assertEquals(o.size(),n.size());
        (0 ..< (o.size())).each { i ->
            def oi = o[i];
            def ni = n[i];
            assertSame(oi.class,ni.class);
            assertEquals(oi.name,ni.name);
            assertEquals(oi.values,ni.values);
        }
    }

    public void testLabelAxes() {
        def p = createMatrixProject();

        List<Slave> slaves = (0..<4).collect { createSlave() }

        p.axes.add(new LabelAxis("label1",[slaves[0].nodeName, slaves[1].nodeName]));
        p.axes.add(new LabelAxis("label2",[slaves[2].nodeName, slaves[3].nodeName]));

        System.out.println(p.labels);
        assertEquals(4,p.labels.size());
        assertTrue(p.labels.contains(hudson.getLabel("slave0&&slave2")));
        assertTrue(p.labels.contains(hudson.getLabel("slave1&&slave2")));
        assertTrue(p.labels.contains(hudson.getLabel("slave0&&slave3")));
        assertTrue(p.labels.contains(hudson.getLabel("slave1&&slave3")));
    }

    /**
     * Quiettng down Hudson causes a dead lock if the parent is running but children is in the queue
     */
    @Bug(4873)
    void testQuietDownDeadlock() {
        def p = createMatrixProject();
        p.axes = new AxisList(new TextAxis("foo","1","2"));
        p.runSequentially = true; // so that we can put the 2nd one in the queue

        OneShotEvent firstStarted = new OneShotEvent();
        OneShotEvent buildCanProceed = new OneShotEvent();

        p.getBuildersList().add( [perform:{ AbstractBuild build, Launcher launcher, BuildListener listener ->
            firstStarted.signal();
            buildCanProceed.block();
            return true;
        }] as TestBuilder );
        Future f = p.scheduleBuild2(0)

        // have foo=1 block to make sure the 2nd configuration is in the queue
        firstStarted.block();
        // enter into the quiet down while foo=2 is still in the queue
        hudson.doQuietDown();
        buildCanProceed.signal();

        // make sure foo=2 still completes. use time out to avoid hang
        assertBuildStatusSuccess(f.get(10,TimeUnit.SECONDS));

        // MatrixProject scheduled after the quiet down shouldn't start
        try {
            Future g = p.scheduleBuild2(0)
            g.get(3,TimeUnit.SECONDS)
            fail()
        } catch (TimeoutException e) {
            // expected
        }        
    }
}
