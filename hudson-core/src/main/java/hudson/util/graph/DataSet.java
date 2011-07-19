/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi, Winston Prakash
 *     
 *
 *******************************************************************************/ 

package hudson.util.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds {@link CategoryDataset}.
 *
 * <p>
 * This code works around an issue in {@link DefaultCategoryDataset} where
 * order of addition changes the way they are drawn.
 *
 * @param <Row>
 *      Names that identify different graphs drawn in the same chart.
 * @param <Column>
 *      X-axis.
 */
public final class DataSet<Row extends Comparable, Column extends Comparable> {

    private List<Number> values = new ArrayList<Number>();
    private List<Row> rows = new ArrayList<Row>();
    private List<Column> columns = new ArrayList<Column>();

    public void add(Number value, Row rowKey, Column columnKey) {
        values.add(value);
        rows.add(rowKey);
        columns.add(columnKey);
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public List<Number> getValues() {
        return values;
    }

    public void setValues(List<Number> values) {
        this.values = values;
    }

}
