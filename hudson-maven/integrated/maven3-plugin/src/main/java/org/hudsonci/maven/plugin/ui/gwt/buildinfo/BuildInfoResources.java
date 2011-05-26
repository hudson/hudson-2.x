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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * {@link ClientBundle} resource for the build information view.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public interface BuildInfoResources
    extends HudsonResources
{
    public static final BuildInfoResources INSTANCE = GWT.create(BuildInfoResources.class);

    @Source("MavenBuildInfoStyle.css")
    Style style();

    @Source("time.png")
    ImageResource activityScheduled();

    @Source("time_go.png")
    ImageResource activityExecuting();

    @Source("arrow_refresh.png")
    ImageResource refresh();

    public interface Style
        extends CssResource
    {
        @ClassName("maven3-moduleResultFAILURE")
        String maven3ModuleResultFAILURE();

        @ClassName("maven3-moduleResultSUCCESS")
        String maven3ModuleResultSUCCESS();

        @ClassName("maven3-moduleResultSKIPPED")
        String maven3ModuleResultSKIPPED();
    }
}
