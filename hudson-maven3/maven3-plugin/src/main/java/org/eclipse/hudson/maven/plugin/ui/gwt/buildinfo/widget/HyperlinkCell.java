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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.widget;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * A cell to render hyperlinks.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class HyperlinkCell
    extends AbstractCell<Hyperlink>
{
    @Override
    public void render( Context context, Hyperlink link, SafeHtmlBuilder sb )
    {
        // Note: toString renders properly but getHTML doesn't; go figure.
        sb.append( SafeHtmlUtils.fromTrustedString( link.toString() ) );
    }
}
