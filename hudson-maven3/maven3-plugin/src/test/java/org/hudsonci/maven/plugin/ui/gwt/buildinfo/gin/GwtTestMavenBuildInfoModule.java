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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo.gin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.view.client.ListDataProvider;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.gin.MavenBuildInfoInjector;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.gin.MavenBuildInfoModule;

public class GwtTestMavenBuildInfoModule extends GWTTestCase
{
    @Override
    public String getModuleName()
    {
        return "org.hudsonci.maven.plugin.ui.gwt.buildinfo.MavenBuildInfo";
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
