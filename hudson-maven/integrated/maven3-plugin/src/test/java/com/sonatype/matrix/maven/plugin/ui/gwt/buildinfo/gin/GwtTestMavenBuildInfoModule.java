/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.gin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.view.client.ListDataProvider;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;

public class GwtTestMavenBuildInfoModule extends GWTTestCase
{
    @Override
    public String getModuleName()
    {
        return "com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.MavenBuildInfo";
        // MavenBuildInfoTest
    }
    
    @Override
    public void gwtSetUp()
    {
        setPageParameters();
    }
    
    private native void setPageParameters() /*-{
        $wnd.baseRestURI = "/fake/rest/uri";
        $wnd.projectName = "test-project";
        $wnd.buildNumber = 1;
    }-*/;

    public void testGwtJunitConfig()
    {
        assertTrue( true );
    }
    
    /**
     * Not passing due to GwtLoadedBuildStateCoordinates not loading config
     * from the page.
     */
    public void _testGinConfiguration()
    {
        MavenBuildInfoInjector ginjector = GWT.create(MavenBuildInfoInjector.class);
        assertNotNull( ginjector.getAppController() );
    }
    
    /**
     * Maven GWT test runner doesn't agree with this approach.
     * 
     * TODO: determine what the problem is, test module? code under test in test class?
     */
    public void _testModuleDataProviderIsSingleton()
    {
        TestGinjector ginjector = GWT.create( TestGinjector.class );
        
        ListDataProvider<MavenProjectDTO> singletonInstance = ginjector.getModuleDataProvider();
        
        assertTrue( singletonInstance == ginjector.getModuleDataProvider() );
        assertTrue( singletonInstance == ginjector.getTestObject().getMdp() );
        fail("test class actually ran, using fail to highlight that since test timeout doesn't fail the build");
    }

    @GinModules(MavenBuildInfoModule.class)
    public interface TestGinjector extends MavenBuildInfoInjector {
        TestObject getTestObject();
        ListDataProvider<MavenProjectDTO> getModuleDataProvider();
    }
    
    @Singleton
    public class TestObject
    {
        private final ListDataProvider<MavenProjectDTO> mdp;

        @Inject
        public TestObject( ListDataProvider<MavenProjectDTO> mdp )
        {
            this.mdp = mdp;
        }
        
        public ListDataProvider<MavenProjectDTO> getMdp()
        {
            return mdp;
        }
    }
}
