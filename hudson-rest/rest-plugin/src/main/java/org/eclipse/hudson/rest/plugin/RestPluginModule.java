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

package org.eclipse.hudson.rest.plugin;

import com.google.inject.AbstractModule;
import hudson.Plugin;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.hudson.events.EventConsumer;
import org.eclipse.hudson.events.ready.ReadyEvent;
import org.eclipse.hudson.rest.common.ObjectMapperProvider;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.EventObject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Additional Guice bindings for the REST plugin.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
public class RestPluginModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class);
    }
}
