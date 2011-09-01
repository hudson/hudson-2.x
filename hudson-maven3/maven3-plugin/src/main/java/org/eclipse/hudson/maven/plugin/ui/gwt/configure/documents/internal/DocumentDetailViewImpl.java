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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.HasData;

import org.eclipse.hudson.gwt.common.EnumListBox;
import org.eclipse.hudson.gwt.common.MaximizedCellTable;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentDetailPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentDetailView;
import org.eclipse.hudson.maven.model.config.DocumentAttributeDTO;
import org.eclipse.hudson.maven.model.config.DocumentTypeDTO;

import javax.inject.Inject;
import javax.inject.Singleton;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_RIGHT;

/**
 * Default implementation of {@link DocumentDetailView}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class DocumentDetailViewImpl
    extends ResizeComposite
    implements DocumentDetailView
{
    private final MessagesResource messages;

    private final TextBox idText;

    private final EnumListBox<DocumentTypeDTO> typeBox;

    private final TextBox nameText;

    private final TextArea descriptionText;

    private final CellTable<DocumentAttributeDTO> attributesTable;

    private final TextArea contentText;

    private final Button saveButton;

    private final Button cancelButton;

    private DocumentDetailPresenter presenter;

    @Inject
    public DocumentDetailViewImpl(final MessagesResource messages) {
        this.messages = checkNotNull(messages);

        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
        initWidget(dockPanel);
        ensureDebugId("document-detail-view");

        // TODO: Add hooks(efficient) hooks to mark record as dirty

        // FIXME: Need to work on this layout so its easier on the eyes an sizes properly

        // Fields
        int row = 0;
        FlexTable fieldsTable = new FlexTable();
        fieldsTable.setWidth("100%");
        fieldsTable.setCellSpacing(4);
        fieldsTable.setCellPadding(0);
        dockPanel.addNorth(fieldsTable, 20.0);

        Label idLabel = new Label(messages.id());
        idLabel.setHorizontalAlignment(ALIGN_RIGHT);
        fieldsTable.setWidget(row, 0, idLabel);

        idText = new TextBox();
        idText.setWidth("100%");
        idText.setReadOnly(true);
        fieldsTable.setWidget(row, 1, idText);

        row++;
        Label typeLabel = new Label(messages.type());
        typeLabel.setHorizontalAlignment(ALIGN_RIGHT);
        fieldsTable.setWidget(row, 0, typeLabel);

        typeBox = new EnumListBox<DocumentTypeDTO>(DocumentTypeDTO.class, DocumentTypeDTO.SETTINGS);
        fieldsTable.setWidget(row, 1, typeBox);

        row++;
        Label nameLabel = new Label(messages.name());
        nameLabel.setHorizontalAlignment(ALIGN_RIGHT);
        fieldsTable.setWidget(row, 0, nameLabel);

        nameText = new TextBox();
        nameText.setWidth("100%");
        fieldsTable.setWidget(row, 1, nameText);

        row++;
        Label descriptionLabel = new Label(messages.description());
        descriptionLabel.setHorizontalAlignment(ALIGN_RIGHT);
        fieldsTable.setWidget(row, 0, descriptionLabel);

        descriptionText = new TextArea();
        descriptionText.setWidth("100%");
        descriptionText.setVisibleLines(4);
        fieldsTable.setWidget(row, 1, descriptionText);

        // Attributes
        row++;
        Label attributesLabel = new Label(messages.attributes());
        attributesLabel.setHorizontalAlignment(ALIGN_RIGHT);
        fieldsTable.setWidget(row, 0, attributesLabel);

        attributesTable = new MaximizedCellTable<DocumentAttributeDTO>(10);
        attributesTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

        TextColumn<DocumentAttributeDTO> nameColumn = new TextColumn<DocumentAttributeDTO>()
        {
            public String getValue(final DocumentAttributeDTO value) {
                return value.getName();
            }
        };
        attributesTable.addColumn(nameColumn);

        TextColumn<DocumentAttributeDTO> valueColumn = new TextColumn<DocumentAttributeDTO>()
        {
            public String getValue(final DocumentAttributeDTO value) {
                return value.getValue();
            }
        };
        attributesTable.addColumn(valueColumn);

        ScrollPanel attributesTableScroller = new ScrollPanel();
        attributesTableScroller.setSize("100%", "100%");
        // WORK AROUND: Seems like we have to always show the bar here, or the layout gets all messed up :-\
        // attributesTableScroller.setAlwaysShowScrollBars(true); leaving out for now to see if we can fix... pffff
        attributesTableScroller.setWidget(attributesTable);
        fieldsTable.setWidget(row, 1, attributesTableScroller);

        // Content
        contentText = new TextArea();
        contentText.setSize("100%", "100%");

        ScrollPanel contentScroller = new ScrollPanel();
        contentScroller.setSize("100%", "100%");
        contentScroller.setWidget(contentText);
        dockPanel.add(contentScroller);

        // Bottom buttons
        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.setSize("100%", "100%");
        dockPanel.addSouth(buttonPanel, 2.0);

        saveButton = new Button(messages.save());
        saveButton.addClickHandler(new ClickHandler()
        {
            public void onClick(final ClickEvent event) {
                presenter.doSave();
            }
        });
        buttonPanel.add(saveButton);

        cancelButton = new Button(messages.cancel());
        cancelButton.addClickHandler(new ClickHandler()
        {
            public void onClick(final ClickEvent event) {
                presenter.doCancel();
            }
        });
        buttonPanel.add(cancelButton);
    }

    public void setPresenter(final DocumentDetailPresenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    public void setId(final String value) {
        idText.setText(value);
    }

    public String getId() {
        return idText.getText();
    }

    public void setType(final DocumentTypeDTO value) {
        typeBox.setSelected(value);
    }

    public DocumentTypeDTO getType() {
        return typeBox.getSelected();
    }

    public void setName(final String value) {
        nameText.setText(value);
    }

    public String getName() {
        return nameText.getText();
    }

    public void setDescription(final String value) {
        descriptionText.setText(value);
    }

    public String getDescription() {
        return descriptionText.getText();
    }

    public HasData<DocumentAttributeDTO> getAttributesDataContainer() {
        return attributesTable;
    }

    public void setContent(final String value) {
        contentText.setText(value);
    }

    public String getContent() {
        return contentText.getText();
    }

    public void setNewDocument(final boolean flag) {
        if (flag) {
            saveButton.setText(messages.save());
            saveButton.setTitle(messages.save()); // FIXME: Use sep message for tool-tip
            cancelButton.setText(messages.cancel());
            cancelButton.setTitle(messages.cancel()); // FIXME: Use sep message for tool-tip
        }
        else {
            saveButton.setText(messages.update());
            saveButton.setTitle(messages.update()); // FIXME: Use sep message for tool-tip
            cancelButton.setText(messages.revert());
            cancelButton.setTitle(messages.revert()); // FIXME: Use sep message for tool-tip
        }
    }

    public void clear() {
        setId(null);
        setType(null);
        setName(null);
        setDescription(null);
        setContent(null);
    }
}
