package hudson.maven;

import org.eclipse.hudson.legacy.maven.plugin.*;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.MojoInfo
 */
public class MojoInfo extends org.eclipse.hudson.legacy.maven.plugin.MojoInfo {

    public MojoInfo(MojoExecution mojoExecution, Mojo mojo, PlexusConfiguration configuration, ExpressionEvaluator expressionEvaluator) {
        super(mojoExecution, mojo, configuration, expressionEvaluator);
    }
}
