/*******************************************************************************
 *
 * Copyright (c) 2010, CloudBees, Inc.
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

package hudson.model;

/**
 * {@link Item} can return this from the "getIconColor" method so that
 * its "status icon" can be shown in Hudson UI.
 *
 * <p>
 * For future compatibility, please extend from {@link AbstractStatusIcon}
 * instead of implementing this directly, so that we can add methods later.
 *
 * <p>
 * This is a generalization of {@link BallColor}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.390
 * @see StockStatusIcon
 */
public interface StatusIcon {
    /**
     * Returns the URL to the image.
     *
     * @param size
     *      The size specified. Must support "16x16", "24x24", and "32x32" at least.
     *      For forward compatibility, if you receive a size that's not supported,
     *      consider returning your biggest icon (and let the browser rescale.)
     * @return
     *      The URL is rendered as is in the img @src attribute, so it must contain
     *      the context path, etc.
     */
    String getImageOf(String size);

    /**
     * Gets the human-readable description used as img/@alt.
     */
    String getDescription();
}
