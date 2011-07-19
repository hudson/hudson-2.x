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

package org.eclipse.hudson.rest.client.internal;

import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.hudson.rest.client.HudsonClient;
import org.eclipse.hudson.rest.client.ext.AdminClient;
import org.eclipse.hudson.rest.client.ext.BuildClient;
import org.eclipse.hudson.rest.client.ext.NodeClient;
import org.eclipse.hudson.rest.client.ext.NotificationClient;
import org.eclipse.hudson.rest.client.ext.ProjectClient;
import org.eclipse.hudson.rest.client.ext.QueueClient;
import org.eclipse.hudson.rest.client.ext.StatusClient;
import org.eclipse.hudson.rest.client.ext.UserClient;
import org.eclipse.hudson.rest.client.ext.maven.BuildStateClient;
import org.eclipse.hudson.rest.client.ext.maven.BuilderConfigClient;
import org.eclipse.hudson.rest.client.ext.maven.BuilderDefaultConfigClient;
import org.eclipse.hudson.rest.client.ext.maven.DocumentClient;
import org.eclipse.hudson.rest.client.internal.ext.AdminClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.BuildClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.NodeClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.NotificationClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.ProjectClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.QueueClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.StatusClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.UserClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.maven.BuildStateClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.maven.BuilderConfigClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.maven.BuilderDefaultConfigClientImpl;
import org.eclipse.hudson.rest.client.internal.ext.maven.DocumentClientImpl;
import org.eclipse.hudson.rest.client.internal.jersey.SisuComponentProviderFactory;
import org.eclipse.hudson.rest.common.JacksonCodec;
import org.eclipse.hudson.rest.common.JacksonProvider;
import org.eclipse.hudson.rest.common.JsonCodec;
import org.eclipse.hudson.rest.common.ObjectMapperProvider;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import javax.inject.Named;

import static com.google.inject.name.Names.named;

/**
 * Hudson client module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
public class HudsonClientModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        bind(IoCComponentProviderFactory.class).annotatedWith(named("sisu")).to(SisuComponentProviderFactory.class);
        bind(HudsonClient.class).to(HudsonClientImpl.class);
        bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class);
        bind(JsonCodec.class).to(JacksonCodec.class);
        bind(JacksonProvider.class);

        // Bind extensions by their names
        bind(AdminClient.class, AdminClientImpl.class);
        bind(BuildClient.class, BuildClientImpl.class);
        bind(ProjectClient.class, ProjectClientImpl.class);
        bind(NodeClient.class, NodeClientImpl.class);
        bind(NotificationClient.class, NotificationClientImpl.class);
        bind(QueueClient.class, QueueClientImpl.class);
        bind(StatusClient.class, StatusClientImpl.class);
        bind(UserClient.class, UserClientImpl.class);

        // TODO: is this a good idea, maven plugin resource clients in generic rest client module? (jdillon: no, but they are fine here for now)
        bind(BuildStateClient.class, BuildStateClientImpl.class);
        bind(BuilderConfigClient.class, BuilderConfigClientImpl.class);
        bind(BuilderDefaultConfigClient.class, BuilderDefaultConfigClientImpl.class);
        bind(DocumentClient.class, DocumentClientImpl.class);
    }

    private <T extends HudsonClient.Extension> void bind(final Class<T> key, final Class<? extends T> impl) {
        bind(HudsonClient.Extension.class).annotatedWith(named(key.getName())).to(impl);
    }
}
