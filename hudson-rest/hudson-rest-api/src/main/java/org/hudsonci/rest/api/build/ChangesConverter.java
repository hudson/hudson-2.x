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

package org.hudsonci.rest.api.build;

import javax.inject.Inject;

import org.hudsonci.rest.api.internal.ConverterSupport;
import org.hudsonci.rest.api.user.UserConverter;

import org.hudsonci.rest.model.build.ChangeEntryDTO;
import org.hudsonci.rest.model.build.ChangeFileDTO;
import org.hudsonci.rest.model.build.ChangeTypeDTO;
import org.hudsonci.rest.model.build.ChangesDTO;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;

/**
 * Converts {@link hudson.scm.ChangeLogSet} into {@link ChangesDTO} object.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ChangesConverter
    extends ConverterSupport
{
    @Inject
    private UserConverter userx;

    public ChangesDTO convert(final ChangeLogSet<?> source) {
        assert source != null;

        log.trace("Converting: {}", source);

        ChangesDTO target = new ChangesDTO();
        target.setKind(source.getKind());

        for (ChangeLogSet.Entry entry : source) {
            target.getEntries().add(convert(entry));
        }

        return target;
    }

    public ChangeEntryDTO convert(final ChangeLogSet.Entry source) {
        assert source != null;

        log.trace("Converting: {}", source);

        ChangeEntryDTO target = new ChangeEntryDTO();

        // TODO: See if we need to pass both raw and annotated messages back?
        // target.setMessage(source.getMsg());

        // FIXME: need to html-decode?
        target.setMessage(source.getMsgAnnotated());
        target.setAuthor(userx.convert(source.getAuthor()));

        for (String path : source.getAffectedPaths()) {
            target.getPaths().add(path);
        }

        try {
            for (ChangeLogSet.AffectedFile file : source.getAffectedFiles()) {
                target.getFiles().add(convert(file));
            }
        }
        catch (UnsupportedOperationException e) {
            // scm does not support getAffectedFiles()
        }

        return target;
    }

    public ChangeFileDTO convert(final ChangeLogSet.AffectedFile source) {
        assert source != null;

        log.trace("Converting: {}", source);

        ChangeFileDTO target = new ChangeFileDTO();
        target.setPath(source.getPath());
        target.setType(convert(source.getEditType()));

        return target;
    }

    public ChangeTypeDTO convert(final EditType source) {
        assert source != null;

        log.trace("Converting: {}", source);

        return ChangeTypeDTO.valueOf(source.getName().toUpperCase());
    }
}
