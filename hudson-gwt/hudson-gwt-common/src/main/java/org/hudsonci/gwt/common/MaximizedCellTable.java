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
