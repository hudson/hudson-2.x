/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget;

import com.google.gwt.user.cellview.client.TextColumn;
import com.sonatype.matrix.gwt.common.MaximizedCellTable;
import com.sonatype.matrix.maven.model.PropertiesDTO;
import com.sonatype.matrix.maven.model.PropertiesDTO.Entry;

/**
 * Table to display {@link PropertiesDTO.Entry} items.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
public class PropertiesTable extends MaximizedCellTable<Entry> 
{
    public PropertiesTable()
    {
        createColumns();
    }
    
    private void createColumns()
    {
        addColumn( new TextColumn<Entry>()
        {
            @Override
            public String getValue( Entry entry )
            {
                return entry.getName();
            }
        }, "Name" );
        
        addColumn( new TextColumn<Entry>()
        {
            @Override
            public String getValue( Entry entry )
            {
                return entry.getValue();
            }
        }, "Value" );
    }
}
