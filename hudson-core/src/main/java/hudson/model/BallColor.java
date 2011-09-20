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
*    Kohsuke Kawaguchi, Simon Wiest
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.util.ColorPalette;
import org.jvnet.localizer.LocaleProvider;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.Stapler;

import java.awt.Color;

import java.util.Locale;

/**
 * Ball color used for the build status indication.
 *
 * <p>
 * There are four basic colors, plus their animated "bouncy" versions.
 * {@link #ordinal()} is the sort order. 
 *
 * <p>
 * Note that multiple {@link BallColor} instances may map to the same
 * RGB color, to avoid the rainbow effect.
 *
 * <h2>Historical Note</h2>
 * <p>
 * Hudson started to overload colors &mdash; for example grey could mean
 * either disabled, aborted, or not yet built. As a result, {@link BallColor}
 * becomes more like a "logical" color, in the sense that different {@link BallColor}
 * values can map to the same RGB color. See issue #956.
 *
 * @author Kohsuke Kawaguchi
 */
public enum BallColor implements StatusIcon {
    RED("red",Messages._BallColor_Failed(), ColorPalette.RED),
    RED_ANIME("red_anime",Messages._BallColor_InProgress(), ColorPalette.RED),
    YELLOW("yellow",Messages._BallColor_Unstable(), ColorPalette.YELLOW),
    YELLOW_ANIME("yellow_anime",Messages._BallColor_InProgress(), ColorPalette.YELLOW),
    BLUE("blue",Messages._BallColor_Success(), ColorPalette.BLUE),
    BLUE_ANIME("blue_anime",Messages._BallColor_InProgress(), ColorPalette.BLUE),
    // for historical reasons they are called grey.
    GREY("grey",Messages._BallColor_Pending(), ColorPalette.GREY),
    GREY_ANIME("grey_anime",Messages._BallColor_InProgress(), ColorPalette.GREY),

    DISABLED("grey",Messages._BallColor_Disabled(), ColorPalette.GREY),
    DISABLED_ANIME("grey_anime",Messages._BallColor_InProgress(), ColorPalette.GREY),
    ABORTED("grey",Messages._BallColor_Aborted(), ColorPalette.GREY),
    ABORTED_ANIME("grey_anime",Messages._BallColor_InProgress(), ColorPalette.GREY),
    ;

    private final Localizable description;
    private final String image;
    private final Color baseColor;

    BallColor(String image, Localizable description, Color baseColor) {
        this.baseColor = baseColor;
        // name() is not usable in the constructor, so I have to repeat the name twice
        // in the constants definition.
        this.image = image+ (image.endsWith("_anime")?".gif":".png");
        this.description = description;
    }

    /**
     * String like "red.png" that represents the file name of the image.
     */
    public String getImage() {
        return image;
    }

    public String getImageOf(String size) {
        return Stapler.getCurrentRequest().getContextPath()+Hudson.RESOURCE_PATH+"/images/"+size+'/'+image;
    }

    /**
     * Gets the human-readable description used as img/@alt.
     */
    public String getDescription() {
        return description.toString(LocaleProvider.getLocale());
    }

    /**
     * Gets the RGB color of this color. Animation effect is not reflected to this value.
     */
    public Color getBaseColor() {
        return baseColor;
    }

    /**
     * Returns the {@link #getBaseColor()} in the "#RRGGBB" format.
     */
    public String getHtmlBaseColor() {
        return String.format("#%06X",baseColor.getRGB()&0xFFFFFF);
    }

    /**
     * Also used as a final name.
     */
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Gets the animated version.
     */
    public BallColor anime() {
        if(isAnimated())   return this;
        else               return valueOf(name()+"_ANIME");
    }

    /**
     * Gets the unanimated version.
     */
    public BallColor noAnime() {
        if(isAnimated())   return valueOf(name().substring(0,name().length()-6));
        else               return this;
    }

    /**
     * True if the icon is animated.
     */
    public boolean isAnimated() {
        return name().endsWith("_ANIME");
    }
}
