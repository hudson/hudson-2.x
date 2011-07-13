/*
 * The MIT License
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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
package hudson.maven.reporters;

import org.eclipse.hudson.legacy.maven.plugin.MavenBuild;
import org.eclipse.hudson.legacy.maven.plugin.MavenProjectActionBuilder;
import hudson.model.Result;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.HudsonTestCase;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSetBuild;
import org.eclipse.hudson.legacy.maven.plugin.reporters.SurefireArchiver;

/**
 * @author Kohsuke Kawaguchi
 */
public class SurefireArchiverTest extends HudsonTestCase {

    public void testSerialization() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m = createMavenProject();
        m.setScm(new ExtractResourceSCM(getClass().getResource("../maven-surefire-unstable.zip")));
        m.setGoals("install");

        MavenModuleSetBuild b = m.scheduleBuild2(0).get();
        assertBuildStatus(Result.UNSTABLE, b);


        MavenBuild mb = b.getModuleLastBuilds().values().iterator().next();
        boolean foundFactory = false, foundSurefire = false;
        for (MavenProjectActionBuilder x : mb.getProjectActionBuilders()) {
            if (x instanceof SurefireArchiver.FactoryImpl) { 
                foundFactory = true;
            }
            if (x instanceof SurefireArchiver) {
                foundSurefire = true;
            }
        }

        assertTrue(foundFactory);
        assertFalse(foundSurefire);
    }
}
