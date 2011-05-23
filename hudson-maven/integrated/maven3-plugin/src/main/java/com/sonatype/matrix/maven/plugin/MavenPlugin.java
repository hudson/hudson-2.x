/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin;

import com.google.inject.Key;
import com.sonatype.matrix.common.marshal.XStreamMarshaller;
import com.sonatype.matrix.common.marshal.xref.FileXReferenceStore;
import com.sonatype.matrix.common.marshal.xref.XReferenceConverter;
import com.sonatype.matrix.common.marshal.xref.XReferenceStoreConverter;
import com.sonatype.matrix.license.LicenseManager;
import com.sonatype.matrix.maven.model.UniqueList;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.plugin.artifactrecorder.ArtifactArchiver;
import com.sonatype.matrix.maven.plugin.artifactrecorder.ArtifactFingerprinter;
import com.sonatype.matrix.maven.plugin.builder.BuildStateRecord;
import com.sonatype.matrix.maven.plugin.builder.MavenBuildAction;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilder;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderDescriptor;
import com.sonatype.matrix.maven.plugin.dependencymonitor.DependencyNotifier;
import com.sonatype.matrix.maven.plugin.dependencymonitor.DependencyTrigger;
import com.sonatype.matrix.maven.plugin.install.MavenInstallation;
import com.sonatype.matrix.maven.plugin.internal.MavenFeature;
import org.hudsonci.inject.Smoothie;
import com.thoughtworks.xstream.XStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.licensing.LicensingException;

import hudson.Plugin;
import hudson.XmlFile;
import hudson.model.Items;
import hudson.model.Run;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Matrix Maven plugin.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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

    @Override
    public void postInitialize() throws Exception {
        // FIXME: CTOR injection of this fails with binding failures
        LicenseManager licenseManager = Smoothie.getContainer().get(Key.get(LicenseManager.class));

        // Check for license validity
        try {
            licenseManager.validate(MavenFeature.INSTANCE);
        }
        catch (LicensingException e) {
            log.error("Unlicensed feature", e);
        }
    }
}
