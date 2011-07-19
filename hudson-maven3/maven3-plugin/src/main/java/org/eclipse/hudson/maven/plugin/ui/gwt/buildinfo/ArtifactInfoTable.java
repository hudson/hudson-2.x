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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo;

import org.eclipse.hudson.gwt.common.MaximizedCellTable;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.widget.ArtifactCoordinatesColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import org.eclipse.hudson.maven.model.state.ArtifactActionDTO;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

/**
 * Table to display {@link ArtifactDTO} information.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
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
                if (null == createdProject || createdProject.length() == 0) {
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
