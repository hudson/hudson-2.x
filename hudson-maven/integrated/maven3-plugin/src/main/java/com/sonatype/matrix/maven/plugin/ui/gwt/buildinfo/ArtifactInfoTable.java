/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.user.cellview.client.TextColumn;
import com.sonatype.matrix.gwt.common.MaximizedCellTable;
import com.sonatype.matrix.maven.model.state.ArtifactActionDTO;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget.ArtifactCoordinatesColumn;

/**
 * Table to display {@link ArtifactDTO} information.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
public class ArtifactInfoTable
    extends MaximizedCellTable<ArtifactDTO>
{
    public ArtifactInfoTable() {
        super();
        createColumns();
    }

    private void createColumns() {
        addColumn(new ArtifactCoordinatesColumn(), "Coordinates");

        addColumn(new TextColumn<ArtifactDTO>()
        {
            @Override
            public String getValue(ArtifactDTO artifact) {
                String createdProject = artifact.getCreatedProject();
                if (null == createdProject || createdProject.isEmpty()) {
                    createdProject = "external project";
                }

                return createdProject;
            }
        }, "Created By");

        addColumn(new TextColumn<ArtifactDTO>()
        {
            @Override
            public String getValue(ArtifactDTO artifact) {
                return artifact.getType();
            }
        }, "Type");
    }

    private void createOperationsColumn() {
        addColumn(new TextColumn<ArtifactDTO>()
        {
            @Override
            public String getValue(ArtifactDTO artifact) {
                StringBuilder sb = new StringBuilder();
                for (ArtifactActionDTO action : artifact.getActions()) {
                    sb.append(action.getOperation()).append(" ");
                }

                sb.deleteCharAt(sb.length() - 1);

                return sb.toString();
            }
        }, "Operations");
    }
}
