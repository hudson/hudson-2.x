/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.maven.plugin.builder.internal;

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
