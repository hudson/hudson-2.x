/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.maven.plugin.builder.internal;

/**
 * Maven constants.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public interface MavenConstants
{
    String JAVA_HOME = "JAVA_HOME";

    String JAVA_IO_TMPDIR = "java.io.tmpdir";

    String M2_HOME = "M2_HOME";

    String MAVEN_SKIP_RC = "MAVEN_SKIP_RC";

    String MAVEN_OPTS = "MAVEN_OPTS";

    String MAVEN_REPO_LOCAL = "maven.repo.local";

    String MAVEN_REPO = ".maven/repo";

    String MAVEN_TMP = ".maven/tmp";

    String MAVEN_EXT_CLASS_PATH = "maven.ext.class.path";

    String MAVEN_TERMINATE_CMD = "MAVEN_TERMINATE_CMD";

    String MAVEN_BATCH_ECHO = "MAVEN_BATCH_ECHO";

    String MAVEN_BATCH_PAUSE = "MAVEN_BATCH_PAUSE";

    String ON = "on";

    String OFF = "off";

    String TRUE = Boolean.TRUE.toString();
}
