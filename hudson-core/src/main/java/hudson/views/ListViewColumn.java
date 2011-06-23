/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi, Martin Eigenbrodt
 *     
 *
 *******************************************************************************/ 

package hudson.views;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ListView;
import hudson.model.View;
import hudson.util.DescriptorList;
import org.kohsuke.stapler.export.Exported;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extension point for adding a column to a table rendering of {@link Item}s, such as {@link ListView}.
 *
 * <p>
 * This object must have the <tt>column.jelly</tt>. This view
 * is called for each cell of this column. The {@link Item} object
 * is passed in the "job" variable. The view should render
 * the &lt;td> tag.
 *
 * <p>
 * This object may have an additional <tt>columHeader.jelly</tt>. The default ColmnHeader
 * will render {@link #getColumnCaption()}.
 *
 * <p>
 * If you opt to {@linkplain ListViewColumnDescriptor#shownByDefault() be shown by default},
 * there also must be a default constructor, which is invoked to create a list view column in
 * the default configuration.
 *
 * <p>
 * Originally, this extension point was designed for {@link ListView}, but since then
 * it has grown to be applicable to other {@link View}s and {@link ItemGroup}s that render
 * a collection of {@link Item}s in a tabular format.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.279
 * @see ListViewColumnDescriptor
 */
public abstract class ListViewColumn implements ExtensionPoint, Describable<ListViewColumn> {
    /**
     * Returns the name of the column that explains what this column means
     *
     * @return
     *      The convention is to use capitalization like "Foo Bar Zot".
     */
    @Exported
    public String getColumnCaption() {
        return getDescriptor().getDisplayName();
    }

    /**
     * Returns all the registered {@link ListViewColumn} descriptors.
     */
    public static DescriptorExtensionList<ListViewColumn, Descriptor<ListViewColumn>> all() {
        return Hudson.getInstance().<ListViewColumn, Descriptor<ListViewColumn>>getDescriptorList(ListViewColumn.class);
    }

    /**
     * All registered {@link ListViewColumn}s.
     * @deprecated as of 1.281
     *      Use {@link #all()} for read access and {@link Extension} for registration.
     */
    public static final DescriptorList<ListViewColumn> LIST = new DescriptorList<ListViewColumn>(ListViewColumn.class);

    /**
     * Whether this column will be shown by default.
     * The default implementation is true.
     *
     * @since 1.301
     * @deprecated as of 1.342.
     *      Use {@link ListViewColumnDescriptor#shownByDefault()}
     */
    public boolean shownByDefault() {
        return true;
    }

    /**
     * For compatibility reason, this method may not return a {@link ListViewColumnDescriptor}
     * and instead return a plain {@link Descriptor} instance.
     */
    public Descriptor<ListViewColumn> getDescriptor() {
        return Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * Creates the list of {@link ListViewColumn}s to be used for newly created {@link ListView}s and their likes.
     * @since 1.391
     */
    public static List<ListViewColumn> createDefaultInitialColumnList() {
        // OK, set up default list of columns:
        // create all instances
        ArrayList<ListViewColumn> r = new ArrayList<ListViewColumn>();
        DescriptorExtensionList<ListViewColumn, Descriptor<ListViewColumn>> all = ListViewColumn.all();
        ArrayList<Descriptor<ListViewColumn>> left = new ArrayList<Descriptor<ListViewColumn>>(all);

        for (Class<? extends ListViewColumn> d: DEFAULT_COLUMNS) {
            Descriptor<ListViewColumn> des = all.find(d);
            if (des  != null) {
                try {
                    r.add(des.newInstance(null, null));
                    left.remove(des);
                } catch (FormException e) {
                    LOGGER.log(Level.WARNING, "Failed to instantiate "+des.clazz,e);
                }
            }
        }
        for (Descriptor<ListViewColumn> d : left)
            try {
                if (d instanceof ListViewColumnDescriptor) {
                    ListViewColumnDescriptor ld = (ListViewColumnDescriptor) d;
                    if (!ld.shownByDefault())       continue;   // skip this
                }
                ListViewColumn lvc = d.newInstance(null, null);
                if (!lvc.shownByDefault())      continue; // skip this

                r.add(lvc);
            } catch (FormException e) {
                LOGGER.log(Level.WARNING, "Failed to instantiate "+d.clazz,e);
            }

        return r;
    }

    /**
     * Traditional column layout before the {@link ListViewColumn} becomes extensible.
     */
    private static final List<Class<? extends ListViewColumn>> DEFAULT_COLUMNS =  Arrays.asList(
        StatusColumn.class,
        WeatherColumn.class,
        JobColumn.class,
        LastSuccessColumn.class,
        LastFailureColumn.class,
        LastDurationColumn.class,
        ConsoleColumn.class,
        BuildButtonColumn.class
    );

    private static final Logger LOGGER = Logger.getLogger(ListViewColumn.class.getName());
}
