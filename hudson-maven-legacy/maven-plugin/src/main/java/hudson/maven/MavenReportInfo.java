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
 *
 *******************************************************************************/ 

package hudson.maven;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.reporting.MavenReport;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.MavenReportInfo
 */
public final class MavenReportInfo extends
		org.eclipse.hudson.legacy.maven.plugin.MavenReportInfo {

	public MavenReportInfo(MojoExecution mojoExecution, MavenReport mojo,
			PlexusConfiguration configuration,
			ExpressionEvaluator expressionEvaluator) {
		super(mojoExecution, mojo, configuration, expressionEvaluator);
	}

}
