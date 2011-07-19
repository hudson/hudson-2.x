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

package org.jvnet.hudson.test;

import com.gargoylesoftware.htmlunit.DefaultPageCreator;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.PageCreator;

import java.io.IOException;
import java.util.Locale;

/**
 * {@link PageCreator} that understands JNLP file.
 * 
 * @author Kohsuke Kawaguchi
 */
public class HudsonPageCreator extends DefaultPageCreator {
    @Override
    public Page createPage(WebResponse webResponse, WebWindow webWindow) throws IOException {
        String contentType = webResponse.getContentType().toLowerCase(Locale.ENGLISH);
        if(contentType.equals("application/x-java-jnlp-file"))
            return createXmlPage(webResponse, webWindow);
        return super.createPage(webResponse, webWindow);
    }

    public static final HudsonPageCreator INSTANCE = new HudsonPageCreator();
}
