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

package org.eclipse.hudson.maven.model;

import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;

/**
 * Helper for {@link MavenCoordinatesDTO}.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenCoordinatesDTOHelper
{
    public static final String SNAPSHOT = "SNAPSHOT";

    /**
     * Non-map-style attribute separator.
     */
    public static final String SEPARATOR = ":";

    /**
     * Rendering style for {@link MavenCoordinatesDTO}.
     */
    public static enum RenderStyle
    {
        /**
         * Renders map-style <tt>{g=x,a=x,...}</tt> for each element, missing elements are omitted.
         */
        MAP,

        /**
         * Renders only groupId and artifactId using ":"-style.
         */
        GA,

        /**
         * Renders only groupId, artifactId and version using ":"-style, missing elements render as <tt>null</tt>.
         */
        GAV,

        /**
         * Renders only groupId, artifactId, type and version using ":"-style, missing elements render as <tt>null</tt>.
         */
        GATV,

        /**
         * All elements rendered, rendering <tt>null</tt> where missing using ":"-style, missing elements render as <tt>null</tt>.
         */
        GATCV,

        /**
         * GATCV style where optional TCV bits are excluded if missing, using ":"-style.
         */
        GATCV_OPTIONAL
    }

    /**
     * Determine the style of the given rendered coordinates.  This will not work for {@link RenderStyle#GATCV_OPTIONAL} rendered values.
     *
     * These are "best-guesses" certainly not definitive.
     */
    public static RenderStyle styleOf(final String rendered) {
        if (rendered == null) {
            throw new NullPointerException();
        }

        if (rendered.startsWith("{") && rendered.endsWith("}")) {
            return RenderStyle.MAP;
        }

        String[] elements = rendered.split(SEPARATOR);
        if (elements.length == 2) {
            return RenderStyle.GA;
        }
        else if (elements.length == 3) {
            return RenderStyle.GAV;
        }
        else if (elements.length == 4) {
            return RenderStyle.GATV;
        }
        else if (elements.length == 5) {
            return RenderStyle.GATCV;
        }

        throw new IllegalArgumentException("Unable to determine style of coordinates: " + rendered);
    }

    /**
     * Renders the given {@link MavenCoordinatesDTOHelper} in the requested style.
     */
    public static String asString(final MavenCoordinatesDTO source, final RenderStyle style) {
        assert source != null;

        StringBuilder buff = new StringBuilder();

        switch (style) {
            case MAP:
                buff.append("{");

                buff.append("g=").append(source.getGroupId());
                buff.append(",a=").append(source.getArtifactId());

                if (source.getType() != null) {
                    buff.append(",t=").append(source.getType());
                }
                if (source.getClassifier() != null) {
                    buff.append(",c=").append(source.getClassifier());
                }
                if (source.getVersion() != null) {
                    buff.append(",v=").append(source.getVersion());
                }

                buff.append("}");
                break;

            case GA:
                buff.append(source.getGroupId());
                buff.append(SEPARATOR).append(source.getArtifactId());
                break;

            case GAV:
                buff.append(source.getGroupId());
                buff.append(SEPARATOR).append(source.getArtifactId());
                buff.append(SEPARATOR).append(source.getVersion());
                break;

            case GATV:
                buff.append(source.getGroupId());
                buff.append(SEPARATOR).append(source.getArtifactId());
                buff.append(SEPARATOR).append(source.getType());
                buff.append(SEPARATOR).append(source.getVersion());
                break;

            case GATCV:
                buff.append(source.getGroupId());
                buff.append(SEPARATOR).append(source.getArtifactId());
                buff.append(SEPARATOR).append(source.getType());
                buff.append(SEPARATOR).append(source.getClassifier());
                buff.append(SEPARATOR).append(source.getVersion());
                break;

            case GATCV_OPTIONAL:
                buff.append(source.getGroupId());
                buff.append(SEPARATOR).append(source.getArtifactId());
                if (source.getType() != null) {
                    buff.append(SEPARATOR).append(source.getType());
                }
                if (source.getClassifier() != null) {
                    buff.append(SEPARATOR).append(source.getClassifier());
                }
                if (source.getVersion() != null) {
                    buff.append(SEPARATOR).append(source.getVersion());
                }
                break;
        }

        return buff.toString();
    }

    /**
     * Renders the given {@link MavenCoordinatesDTOHelper} in the {@link RenderStyle#MAP} style.
     */
    public static String asString(final MavenCoordinatesDTO source) {
        return asString(source, RenderStyle.MAP);
    }

    /**
     * Check if the given string is not empty.  Null value is not considered an empty string.
     */
    private static boolean isEmptyString(final String value) {
        return value != null && value.trim().length() == 0;
    }

    /**
     * Normalize all optional values which are empty strings to null.
     */
    public static MavenCoordinatesDTO normalize(final MavenCoordinatesDTO source) {
        assert source != null;

        if (isEmptyString(source.getType())) {
            source.setType(null);
        }
        if (isEmptyString(source.getClassifier())) {
            source.setClassifier(null);
        }
        if (isEmptyString(source.getVersion())) {
            source.setVersion(null);
        }

        return source;
    }

    public static boolean isSnapshot(final MavenCoordinatesDTO source) {
        assert source != null;
        String version = source.getVersion();
        return version != null && version.endsWith(SNAPSHOT);
    }
}
