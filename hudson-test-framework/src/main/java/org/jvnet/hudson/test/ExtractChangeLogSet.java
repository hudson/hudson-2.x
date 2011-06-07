/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Nikita Levyankov
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
package org.jvnet.hudson.test;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;

import java.util.Collection;
import java.util.List;
import java.util.Collections;


/**
 * @author Andrew Bayer
 */
public class ExtractChangeLogSet extends ChangeLogSet<ExtractChangeLogParser.ExtractChangeLogEntry> {
    private List<ExtractChangeLogParser.ExtractChangeLogEntry> changeLogs = null;

    public ExtractChangeLogSet(AbstractBuild<?, ?> build, List<ExtractChangeLogParser.ExtractChangeLogEntry> changeLogs) {
        super(build);
        for (ExtractChangeLogParser.ExtractChangeLogEntry entry : changeLogs) {
            entry.setParent(this);
        }
        this.changeLogs = Collections.unmodifiableList(changeLogs);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<ExtractChangeLogParser.ExtractChangeLogEntry> getLogs() {
        return changeLogs;
    }
}
