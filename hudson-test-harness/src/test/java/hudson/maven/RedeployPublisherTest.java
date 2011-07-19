/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.maven;

import hudson.model.Result;
import hudson.tasks.Maven.MavenInstallation;

import org.apache.commons.io.FileUtils;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;
import org.jvnet.hudson.test.Email;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSetBuild;
import org.eclipse.hudson.legacy.maven.plugin.RedeployPublisher;

/**
 * @author Kohsuke Kawaguchi
 */
public class RedeployPublisherTest extends HudsonTestCase {
    @Bug(2593)
    public void testBug2593() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m2 = createMavenProject();
        File repo = createTmpDir();

        // a fake build
        m2.setScm(new SingleFileSCM("pom.xml",getClass().getResource("big-artifact.pom")));
        m2.getPublishersList().add(new RedeployPublisher("",repo.toURI().toString(),true, false));

        MavenModuleSetBuild b = m2.scheduleBuild2(0).get();
        assertBuildStatus(Result.SUCCESS, b);

        // TODO: confirm that the artifacts use a consistent timestamp
        // TODO: we need to somehow introduce a large delay between deploy since timestamp is only second precision
        // TODO: or maybe we could use a btrace like capability to count the # of invocations?

        System.out.println(repo);
    }

    public void testConfigRoundtrip() throws Exception {
        MavenModuleSet p = createMavenProject();
        RedeployPublisher rp = new RedeployPublisher("theId", "http://some.url/", true, true);
        p.getPublishersList().add(rp);
        submit(new WebClient().getPage(p,"configure").getFormByName("config"));
        assertEqualBeans(rp,p.getPublishersList().get(RedeployPublisher.class),"id,url,uniqueVersion,evenIfUnstable");
    }

//    /**
//     * Makes sure that the webdav wagon component we bundle is compatible.
//     */
//    public void testWebDavDeployment() throws Exception {
//        configureDefaultMaven();
//        MavenModuleSet m2 = createMavenProject();
//
//        // a fake build
//        m2.setScm(new SingleFileSCM("pom.xml",getClass().getResource("big-artifact.pom")));
//        m2.getPublishersList().add(new RedeployPublisher("","dav:http://localhost/dav/",true));
//
//        MavenModuleSetBuild b = m2.scheduleBuild2(0).get();
//        assertBuildStatus(Result.SUCCESS, b);
//    }

    /**
     * Are we having a problem in handling file names with multiple extensions, like ".tar.gz"?
     */
    @Email("http://www.nabble.com/tar.gz-becomes-.gz-after-Hudson-deployment-td25391364.html")
    @Bug(3814)
    public void testTarGz() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m2 = createMavenProject();
        File repo = createTmpDir();

        // a fake build
        m2.setScm(new SingleFileSCM("pom.xml",getClass().getResource("targz-artifact.pom")));
        m2.getPublishersList().add(new RedeployPublisher("",repo.toURI().toString(),false, false));

        MavenModuleSetBuild b = m2.scheduleBuild2(0).get();
        assertBuildStatus(Result.SUCCESS, b);

        assertTrue("tar.gz doesn't exist",new File(repo,"test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-bin.tar.gz").exists());
    }
    
    public void testTarGzUniqueVersionTrue() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m2 = createMavenProject();
        File repo = createTmpDir();
        
        FileUtils.cleanDirectory( repo );
        
        // a fake build
        m2.setScm(new SingleFileSCM("pom.xml",getClass().getResource("targz-artifact.pom")));
        m2.getPublishersList().add(new RedeployPublisher("",repo.toURI().toString(),true, false));

        MavenModuleSetBuild b = m2.scheduleBuild2(0).get();
        assertBuildStatus(Result.SUCCESS, b);
        File artifactDir = new File(repo,"test/test/0.1-SNAPSHOT/");
        String[] files = artifactDir.list( new FilenameFilter()
        {
            
            public boolean accept( File dir, String name )
            {
                return name.endsWith( "tar.gz" );
            }
        });
        assertFalse("tar.gz doesn't exist",new File(repo,"test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-bin.tar.gz").exists());
        assertTrue("tar.gz doesn't exist",!files[0].contains( "SNAPSHOT" ));
    }    
    
    //TODO: Revisit this test case
    public void ignore_testTarGzMaven3() throws Exception {
        
        MavenModuleSet m3 = createMavenProject();
        MavenInstallation mvn = configureMaven3();
        m3.setMaven( mvn.getName() );
        File repo = createTmpDir();
        FileUtils.cleanDirectory( repo );
        // a fake build
        m3.setScm(new SingleFileSCM("pom.xml",getClass().getResource("targz-artifact.pom")));
        m3.getPublishersList().add(new RedeployPublisher("",repo.toURI().toString(),false, false));

        MavenModuleSetBuild b = m3.scheduleBuild2(0).get();
        assertBuildStatus(Result.SUCCESS, b);

        assertTrue( MavenUtil.maven3orLater( b.getMavenVersionUsed() ) );
        File artifactDir = new File(repo,"test/test/0.1-SNAPSHOT/");
        String[] files = artifactDir.list( new FilenameFilter()
        {
            
            public boolean accept( File dir, String name )
            {
                return name.endsWith( "tar.gz" );
            }
        });
        assertFalse("tar.gz doesn't exist",new File(repo,"test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-bin.tar.gz").exists());
        assertTrue("tar.gz doesn't exist",!files[0].contains( "SNAPSHOT" ));
    }    
    
    //TODO - Revisit this test case
    public void ignore_testTarGzUniqueVersionTrueMaven3() throws Exception {
        MavenModuleSet m3 = createMavenProject();
        MavenInstallation mvn = configureMaven3();
        m3.setMaven( mvn.getName() );        
        File repo = createTmpDir();
        FileUtils.cleanDirectory( repo );
        // a fake build
        m3.setScm(new SingleFileSCM("pom.xml",getClass().getResource("targz-artifact.pom")));
        m3.getPublishersList().add(new RedeployPublisher("",repo.toURI().toString(),true, false));

        MavenModuleSetBuild b = m3.scheduleBuild2(0).get();
        assertBuildStatus(Result.SUCCESS, b);
        
        assertTrue( MavenUtil.maven3orLater( b.getMavenVersionUsed() ) );
        
        File artifactDir = new File(repo,"test/test/0.1-SNAPSHOT/");
        String[] files = artifactDir.list( new FilenameFilter()
        {
            
            public boolean accept( File dir, String name )
            {
                return name.endsWith( "tar.gz" );
            }
        });
        assertFalse("tar.gz doesn't exist",new File(repo,"test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-bin.tar.gz").exists());
        assertTrue("tar.gz doesn't exist",!files[0].contains( "SNAPSHOT" ));
    }    

    @Bug(3773)
    public void testDeployUnstable() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m2 = createMavenProject();
        File repo = createTmpDir();
        FileUtils.cleanDirectory( repo );
        // a build with a failing unit tests
        m2.setScm(new ExtractResourceSCM(getClass().getResource("maven-test-failure-findbugs.zip")));
        m2.getPublishersList().add(new RedeployPublisher("",repo.toURI().toString(),false, true));

        MavenModuleSetBuild b = m2.scheduleBuild2(0).get();
        assertBuildStatus(Result.UNSTABLE, b);

        assertTrue("Artifact should have been published even when the build is unstable",
                   new File(repo,"test/test/1.0-SNAPSHOT/test-1.0-SNAPSHOT.jar").exists());
    }
}
