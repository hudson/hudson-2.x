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

package org.hudsonci.rest.client.internal;

import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.hudsonci.rest.client.HudsonClient;
import org.hudsonci.rest.client.ext.AdminClient;
import org.hudsonci.rest.client.ext.BuildClient;
import org.hudsonci.rest.client.ext.NodeClient;
import org.hudsonci.rest.client.ext.NotificationClient;
import org.hudsonci.rest.client.ext.ProjectClient;
import org.hudsonci.rest.client.ext.QueueClient;
import org.hudsonci.rest.client.ext.StatusClient;
import org.hudsonci.rest.client.ext.UserClient;
import org.hudsonci.rest.client.ext.maven.BuildStateClient;
import org.hudsonci.rest.client.ext.maven.BuilderConfigClient;
import org.hudsonci.rest.client.ext.maven.BuilderDefaultConfigClient;
import org.hudsonci.rest.client.ext.maven.DocumentClient;
import org.hudsonci.rest.client.internal.ext.AdminClientImpl;
import org.hudsonci.rest.client.internal.ext.BuildClientImpl;
import org.hudsonci.rest.client.internal.ext.NodeClientImpl;
import org.hudsonci.rest.client.internal.ext.NotificationClientImpl;
import org.hudsonci.rest.client.internal.ext.ProjectClientImpl;
import org.hudsonci.rest.client.internal.ext.QueueClientImpl;
import org.hudsonci.rest.client.internal.ext.StatusClientImpl;
import org.hudsonci.rest.client.internal.ext.UserClientImpl;
import org.hudsonci.rest.client.internal.ext.maven.BuildStateClientImpl;
import org.hudsonci.rest.client.internal.ext.maven.BuilderConfigClientImpl;
import org.hudsonci.rest.client.internal.ext.maven.BuilderDefaultConfigClientImpl;
import org.hudsonci.rest.client.internal.ext.maven.DocumentClientImpl;
import org.hudsonci.rest.client.internal.jersey.SisuComponentProviderFactory;
import org.hudsonci.rest.common.JacksonCodec;
import org.hudsonci.rest.common.JacksonProvider;
import org.hudsonci.rest.common.JsonCodec;
import org.hudsonci.rest.common.ObjectMapperProvider;
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
