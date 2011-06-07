/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
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
package org.jvnet.hudson.test.recipes;

import org.jvnet.hudson.test.HudsonTestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.net.URL;

/**
 * Installs the specified plugin before launching Hudson. 
 *
 * @author Kohsuke Kawaguchi
 */
@Documented
@Recipe(WithPlugin.RunnerImpl.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface WithPlugin {
    /**
     * Name of the plugin.
     *
     * For now, this has to be one of the plugins statically available in resources
     * "/plugins/NAME". TODO: support retrieval through Maven repository.
     */
    String value();

    public class RunnerImpl extends Recipe.Runner<WithPlugin> {
        private WithPlugin a;

        @Override
        public void setup(HudsonTestCase testCase, WithPlugin recipe) throws Exception {
            a = recipe;
            testCase.useLocalPluginManager = true;
        }

        @Override
        public void decorateHome(HudsonTestCase testCase, File home) throws Exception {
            URL res = getClass().getClassLoader().getResource("plugins/" + a.value());
            FileUtils.copyURLToFile(res,new File(home,"plugins/"+a.value()));
        }
    }
}
