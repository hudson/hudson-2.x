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

package org.hudsonci.gwt.common;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

/**
 * Custom button with an image and text.
 *
 * This is a very hacky widget, recommend only using the constructor to configure and not attempt to reconfigure dynamically or bad things happen.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ImageTextButton
    extends Button
{
    private String text;

    // FIXME: This is very hackish... calls to setImage/setText will mess up the button, and the order is important when first setting :-(
    // FIXME: ... based on something I found on the net... so whatever, works for limited use required (sans the update of the text)

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
