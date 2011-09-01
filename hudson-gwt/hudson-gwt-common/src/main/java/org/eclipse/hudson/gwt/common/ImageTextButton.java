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

package org.eclipse.hudson.gwt.common;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

/**
 * Custom button with an image and text.
 *
 * This is a very tricky widget, recommend only using the constructor to configure and not attempt to reconfigure dynamically or bad things happen.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ImageTextButton
    extends Button
{
    private String text;

    // FIXME: Calls to setImage/setText will mess up the button, and the order is important when first setting :-(

    public ImageTextButton(final ImageResource image, final String text) {
        // Order is important here... blah
        setText(text);
        setImage(image);
    }

    public void setImage(final ImageResource image) {
        Image img = new Image(image);
        String definedStyles = img.getElement().getAttribute("style");
        img.getElement().setAttribute("style", definedStyles + "; vertical-align:middle;");
        DOM.insertBefore(getElement(), img.getElement(), DOM.getFirstChild(getElement()));
    }

    @Override
    public void setText(final String text) {
        this.text = text;
        Element span = DOM.createElement("span");
        span.setInnerText(text);
        span.setAttribute("style", "padding-left:3px; vertical-align:middle;");
        DOM.insertChild(getElement(), span, 0);
    }

    @Override
    public String getText() {
        return text;
    }
}
