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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.widget.ArtifactCoordinatesColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import org.hudsonci.gwt.common.MaximizedCellTable;
import org.hudsonci.maven.model.state.ArtifactActionDTO;
import org.hudsonci.maven.model.state.ArtifactDTO;

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
