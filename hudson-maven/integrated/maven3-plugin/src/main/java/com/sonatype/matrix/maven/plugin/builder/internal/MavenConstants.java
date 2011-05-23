/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

/**
 * Maven constants.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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