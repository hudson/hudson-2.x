/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * A cell to render hyperlinks.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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