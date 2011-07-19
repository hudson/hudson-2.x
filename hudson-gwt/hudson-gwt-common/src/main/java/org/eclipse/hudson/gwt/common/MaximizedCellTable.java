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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.ProvidesKey;

/**
 * A {@link CellTable} with a default width of 100%.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class MaximizedCellTable<T>
    extends CellTable<T>
{
    public interface Resources extends CellTable.Resources
    {
        public interface Style extends CellTable.Style {};
        
        @Source({ CellTable.Style.DEFAULT_CSS, DEFAULT_CSS })
        public Style cellTableStyle();
    }
    
    private static Resources DEFAULT_RESOURCES;

    private static final String DEFAULT_CSS = "MaximizedCellTable.css";

    private static final int DEFAULT_PAGESIZE = 15;

    /**
     * Constructs a table with a default page size of 15.
     */
    public MaximizedCellTable()
    {
        this( getDefaultPagesize() );
    }

    /**
     * Constructs a table with the given page size.
     * 
     * @param pageSize the page size
     */
    public MaximizedCellTable( final int pageSize )
    {
        this( pageSize, getDefaultResources(), null );
    }

    /**
     * Constructs a table with a default page size of 15, and the given {@link ProvidesKey key provider}.
     * 
     * @param keyProvider an instance of ProvidesKey<T>, or null if the record
     *            object should act as its own key
     */
    public MaximizedCellTable( ProvidesKey<T> keyProvider )
    {
        this( getDefaultPagesize(), getDefaultResources(), keyProvider );
    }

    /**
     * Constructs a table with the given page size with the specified {@link Resources}.
     * 
     * @param pageSize the page size
     * @param resources the resources to use for this widget
     */
    public MaximizedCellTable( final int pageSize, Resources resources )
    {
        this( pageSize, resources, null );
    }

    /**
     * Constructs a table with the given page size and the given {@link ProvidesKey key provider}.
     * 
     * @param pageSize the page size
     * @param keyProvider an instance of ProvidesKey<T>, or null if the record
     *            object should act as its own key
     */
    public MaximizedCellTable( final int pageSize, ProvidesKey<T> keyProvider )
    {
        this( pageSize, getDefaultResources(), keyProvider );
    }

    /**
     * Constructs a table with the given page size, the specified {@link Resources}, and the given key provider.
     * 
     * @param pageSize the page size
     * @param resources the resources to use for this widget
     * @param keyProvider an instance of ProvidesKey<T>, or null if the record
     *            object should act as its own key
     */
    public MaximizedCellTable( final int pageSize, Resources resources, ProvidesKey<T> keyProvider )
    {
        super( pageSize, resources, keyProvider );
    }

    protected static Resources getDefaultResources()
    {
        if ( DEFAULT_RESOURCES == null )
        {
            DEFAULT_RESOURCES = GWT.create( Resources.class );
        }
        return DEFAULT_RESOURCES;
    }

    protected static int getDefaultPagesize()
    {
        return DEFAULT_PAGESIZE;
    }
}
