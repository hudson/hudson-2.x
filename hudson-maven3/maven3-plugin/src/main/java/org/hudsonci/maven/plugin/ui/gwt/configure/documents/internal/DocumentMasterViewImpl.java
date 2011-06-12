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

package org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import org.hudsonci.gwt.common.ImageResourceColumn;
import org.hudsonci.gwt.common.ImageTextButton;
import org.hudsonci.gwt.common.MaximizedCellTable;
import org.hudsonci.gwt.common.TogglePanel;
import org.hudsonci.gwt.common.ToolBar;
import org.hudsonci.gwt.icons.silk.SilkIcons;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hudsonci.maven.plugin.ui.gwt.configure.documents.Document;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.DocumentDetailView;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.DocumentMasterPresenter;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.DocumentMasterView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link DocumentMasterView}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class DocumentMasterViewImpl
    extends ResizeComposite
    implements DocumentMasterView
{
    private final MessagesResource messages;

    private final ImageTextButton refreshButton;

    private final ImageTextButton addButton;

    private final ImageTextButton removeButton;

    private final CellTable<Document> documentsTable;

    private final ScrollPanel documentsTableScroller;

    private final TogglePanel detailContainer;

    private DocumentMasterPresenter presenter;

    @Inject
    public DocumentMasterViewImpl(final MessagesResource messages, final SilkIcons icons) {
        this.messages = checkNotNull(messages);
        checkNotNull(icons);

        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
        initWidget(dockPanel);
        ensureDebugId("document-master-view");

        // Toolbar
        ToolBar toolBar = new ToolBar();
        toolBar.setSize("100%", "100%");
        dockPanel.addNorth(toolBar, 2.5);

        refreshButton = new ImageTextButton(icons.arrow_refresh(), messages.refresh());
        refreshButton.setTitle(messages.refresh()); // FIXME: Use sep message for tool-tip
        refreshButton.addClickHandler(new ClickHandler()
        {
            public void onClick(final ClickEvent event) {
                presenter.doRefresh();
            }
        });
        toolBar.add(refreshButton);

        addButton = new ImageTextButton(icons.add(), messages.add());
        addButton.setTitle(messages.add()); // FIXME: Use sep message for tool-tip
        addButton.addClickHandler(new ClickHandler()
        {
            public void onClick(final ClickEvent event) {
                presenter.doAdd();
            }
        });
        toolBar.add(addButton);

        removeButton = new ImageTextButton(icons.delete(), messages.remove());
        removeButton.setTitle(messages.remove()); // FIXME: Use sep message for tool-tip
        removeButton.addClickHandler(new ClickHandler()
        {
            public void onClick(final ClickEvent event) {
                presenter.doRemove();
            }
        });
        removeButton.setEnabled(false);
        toolBar.add(removeButton);

        // Content
        SplitLayoutPanel splitPanel = new SplitLayoutPanel();
        dockPanel.add(splitPanel);

        // Documents table
        // FIXME: Only 100 records will be shown, then lost due to assumption of a pager being used
        documentsTable = new MaximizedCellTable<Document>(100, Document.KEY_PROVIDER);
        documentsTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

        ImageResourceColumn<Document> iconColumn = new ImageResourceColumn<Document>()
        {
            @Override
            public ImageResource getValue(final Document document) {
                return document.getIcon();
            }
        };
        documentsTable.addColumn(iconColumn);

        // TODO: Once everything is happy working, might want to not display the ID column
        // TODO: ... its not really important to the user to know what this is here, but still show on detail page for ref
        // TODO: ... this may need to wait until we have form validation to help ensure that a document name is always configured.
        TextColumn<Document> idColumn = new TextColumn<Document>()
        {
            @Override
            public String getValue(final Document document) {
                return document.getId();
            }
        };
        documentsTable.addColumn(idColumn, messages.id());

        TextColumn<Document> typeColumn = new TextColumn<Document>()
        {
            @Override
            public String getValue(final Document document) {
                return document.getType().toString();
            }
        };
        documentsTable.addColumn(typeColumn, messages.type());

        TextColumn<Document> nameColumn = new TextColumn<Document>()
        {
            @Override
            public String getValue(final Document document) {
                return document.getName();
            }
        };
        documentsTable.addColumn(nameColumn, messages.name());

        documentsTableScroller = new ScrollPanel();
        documentsTableScroller.setSize("100%", "100%");
        documentsTableScroller.setWidget(documentsTable);
        splitPanel.addNorth(documentsTableScroller, 200); //px

        // Detail
        detailContainer = new TogglePanel();
        detailContainer.setSize("100%", "100%");
        detailContainer.setSummary(messages.detailSummary());
        splitPanel.add(detailContainer);
    }

    public void setPresenter(final DocumentMasterPresenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    public String getWorkspaceTitle() {
        return messages.documents();
    }

    public HasData<Document> getDocumentDataContainer() {
        return documentsTable;
    }

    public void setDocumentDetailView(final DocumentDetailView view) {
        Widget widget = view.asWidget();
        widget.setSize("100%", "100%");
        // View starts hidden
        widget.setVisible(false);
        detailContainer.setDetail(widget);
    }

    public void setDocumentSelected(final boolean flag) {
        removeButton.setEnabled(flag);
        if (flag) {
            detailContainer.showDetail();
        }
        else {
            detailContainer.showSummary();
        }
    }

    public void scrollToNewDocument() {
        documentsTableScroller.scrollToBottom();
    }
}
