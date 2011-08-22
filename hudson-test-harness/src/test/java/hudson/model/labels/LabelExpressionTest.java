/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       
 *
 *******************************************************************************/ 

package hudson.model.labels;

import antlr.ANTLRException;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Node.Mode;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SequenceLock;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.Future;

/**
 * @author Kohsuke Kawaguchi
 */
public class LabelExpressionTest extends HudsonTestCase {
    /**
     * Verifies the queueing behavior in the presence of the expression.
     */
    public void testQueueBehavior() throws Exception {
        DumbSlave w32 = createSlave("win 32bit",null);
        DumbSlave w64 = createSlave("win 64bit",null);
        createSlave("linux 32bit",null);

        final SequenceLock seq = new SequenceLock();

        FreeStyleProject p1 = createFreeStyleProject();
        p1.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                seq.phase(0); // first, make sure the w32 slave is occupied
                seq.phase(2);
                seq.done();
                return true;
            }
        });
        p1.setAssignedLabel(hudson.getLabel("win && 32bit"));

        FreeStyleProject p2 = createFreeStyleProject();
        p2.setAssignedLabel(hudson.getLabel("win && 32bit"));

        FreeStyleProject p3 = createFreeStyleProject();
        p3.setAssignedLabel(hudson.getLabel("win"));

        Future<FreeStyleBuild> f1 = p1.scheduleBuild2(0);

        seq.phase(1); // we schedule p2 build after w32 slave is occupied
        Future<FreeStyleBuild> f2 = p2.scheduleBuild2(0);

        Thread.sleep(1000); // time window to ensure queue has tried to assign f2 build

        // p3 is tied to 'win', so even though p1 is busy, this should still go ahead and complete
        FreeStyleBuild b3 = assertBuildStatusSuccess(p3.scheduleBuild2(0));
        assertSame(w64,b3.getBuiltOn());

        seq.phase(3);   // once we confirm that p3 build is over, we let p1 proceed

        // p1 should have been built on w32
        FreeStyleBuild b1 = assertBuildStatusSuccess(f1);
        assertSame(w32,b1.getBuiltOn());

        // and so is p2
        FreeStyleBuild b2 = assertBuildStatusSuccess(f2);
        assertSame(w32,b2.getBuiltOn());
    }

    /**
     * Push the build around to different nodes via the assignment
     * to make sure it gets where we need it to.
     */
    public void testQueueBehavior2() throws Exception {
        DumbSlave s = createSlave("win",null);

        FreeStyleProject p = createFreeStyleProject();

        p.setAssignedLabel(hudson.getLabel("!win"));
        FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0));
        assertSame(hudson,b.getBuiltOn());

        p.setAssignedLabel(hudson.getLabel("win"));
        b = assertBuildStatusSuccess(p.scheduleBuild2(0));
        assertSame(s,b.getBuiltOn());

        p.setAssignedLabel(hudson.getLabel("!win"));
        b = assertBuildStatusSuccess(p.scheduleBuild2(0));
        assertSame(hudson,b.getBuiltOn());
    }

    /**
     * Tests the expression parser.
     */
    public void testParser() throws Exception {
        parseAndVerify("foo", "foo");
        parseAndVerify("32bit.dot", "32bit.dot");
        parseAndVerify("foo||bar", "foo || bar");

        // user-given parenthesis is preserved
        parseAndVerify("foo||bar&&zot", "foo||bar&&zot");
        parseAndVerify("foo||(bar&&zot)", "foo||(bar&&zot)");

        parseAndVerify("(foo||bar)&&zot", "(foo||bar)&&zot");
        parseAndVerify("foo->bar", "foo ->\tbar");
        parseAndVerify("!foo<->bar", "!foo <-> bar");
    }

    private void parseAndVerify(String expected, String expr) throws ANTLRException {
        assertEquals(expected, LabelExpression.parseExpression(expr).getName());
    }

    public void testParserError() throws Exception {
        parseShouldFail("foo bar");
        parseShouldFail("foo (bar)");
    }

    public void testLaxParsing() {
        // this should parse as an atom
        LabelAtom l = (LabelAtom)hudson.getLabel("lucene.zones.apache.org (Solaris 10)");
        assertEquals(l.getName(),"lucene.zones.apache.org (Solaris 10)");
        assertEquals(l.getExpression(),"\"lucene.zones.apache.org (Solaris 10)\"");
    }

    public void testDataCompatibilityWithHostNameWithWhitespace() throws Exception {
        DumbSlave slave = new DumbSlave("abc def (xyz) - test", "dummy",
                createTmpDir().getPath(), "1", Mode.NORMAL, "", createComputerLauncher(null), RetentionStrategy.NOOP, Collections.EMPTY_LIST);
        hudson.addNode(slave);


        FreeStyleProject p = createFreeStyleProject();
        p.setAssignedLabel(hudson.getLabel("abc def"));
        assertEquals("abc def",p.getAssignedLabel().getName());
        assertEquals("\"abc def\"",p.getAssignedLabel().getExpression());

        // expression should be persisted, not the name
        Field f = AbstractProject.class.getDeclaredField("assignedNode");
        f.setAccessible(true);
        assertEquals("\"abc def\"",f.get(p));

        // but if the name is set, we'd still like to parse it
        f.set(p,"a:b c");
        assertEquals("a:b c",p.getAssignedLabel().getName());
    }

    public void testQuote() {
        Label l = hudson.getLabel("\"abc\\\\\\\"def\"");
        assertEquals("abc\\\"def",l.getName());
    }

    /**
     * The name should have parenthesis at the right place to preserve the tree structure.
     */
    public void testComposite() {
        LabelAtom x = hudson.getLabelAtom("x");
        assertEquals("!!x",x.not().not().getName());
        assertEquals("(x||x)&&x",x.or(x).and(x).getName());
        assertEquals("x&&x||x",x.and(x).or(x).getName());
    }

    public void ignore_testDash() {
        hudson.getLabelAtom("solaris-x86");
    }

    private void parseShouldFail(String expr) {
        try {
            LabelExpression.parseExpression(expr);
            fail(expr + " should fail to parse");
        } catch (ANTLRException e) {
            // expected
        }
    }

}
