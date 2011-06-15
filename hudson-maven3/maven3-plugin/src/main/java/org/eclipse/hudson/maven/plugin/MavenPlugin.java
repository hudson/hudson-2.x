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

package org.eclipse.hudson.maven.plugin;

import com.google.inject.Key;

import org.eclipse.hudson.inject.Smoothie;
import org.eclipse.hudson.maven.model.UniqueList;
import org.eclipse.hudson.maven.plugin.artifactrecorder.ArtifactArchiver;
import org.eclipse.hudson.maven.plugin.artifactrecorder.ArtifactFingerprinter;
import org.eclipse.hudson.maven.plugin.builder.BuildStateRecord;
import org.eclipse.hudson.maven.plugin.builder.MavenBuildAction;
import org.eclipse.hudson.maven.plugin.builder.MavenBuilder;
import org.eclipse.hudson.maven.plugin.builder.MavenBuilderDescriptor;
import org.eclipse.hudson.maven.plugin.dependencymonitor.DependencyNotifier;
import org.eclipse.hudson.maven.plugin.dependencymonitor.DependencyTrigger;
import org.eclipse.hudson.maven.plugin.install.MavenInstallation;
import org.eclipse.hudson.utils.marshal.XStreamMarshaller;
import org.eclipse.hudson.utils.marshal.xref.FileXReferenceStore;
import org.eclipse.hudson.utils.marshal.xref.XReferenceConverter;
import org.eclipse.hudson.utils.marshal.xref.XReferenceStoreConverter;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;

import com.thoughtworks.xstream.XStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Plugin;
import hudson.XmlFile;
import hudson.model.Items;
import hudson.model.Run;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Hudson Maven plugin.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class MavenPlugin
    extends Plugin
{
    private static final Logger log = LoggerFactory.getLogger(MavenPlugin.class);

    @Override
    public void start() throws Exception {
        // Setup the marshaller which will handle persisting referenced data
        // Do not use XStream2... causes problems with @XStreamImplicit (used in UniqueList)
        XStream xs = new XStream();
        xs.setClassLoader(getClass().getClassLoader());
        xs.processAnnotations(new Class[] {
            BuildStateDTO.class,
            UniqueList.class
        });
        XStreamMarshaller marshaller = new XStreamMarshaller(xs);

        // Setup the reference converter
        FileXReferenceStore store = new FileXReferenceStore(marshaller);
        XReferenceStoreConverter converter = new XReferenceStoreConverter(
                store, Run.XSTREAM.getMapper(), Run.XSTREAM.getReflectionProvider())
        {
            @Override
            public boolean canConvert(final Class type) {
                return type == BuildStateRecord.StateReference.class;
            }
        };
        converter.setHolderType(XReferenceConverter.HolderType.SOFT);

        // Configure the standard xstream instance to know about our converter plus type configuration
        Run.XSTREAM.registerConverter(converter);

        Run.XSTREAM.processAnnotations(new Class[] {
            MavenBuildAction.class,
            BuildStateRecord.class
        });

        Items.XSTREAM.processAnnotations(new Class[] {
            MavenBuilder.class,
            DependencyNotifier.class,
            DependencyTrigger.class,
            ArtifactFingerprinter.class,
            ArtifactArchiver.class
        });

        XmlFile.DEFAULT_XSTREAM.processAnnotations(new Class[] {
            MavenBuilderDescriptor.class,
            MavenInstallation.class
        });
    }
}
