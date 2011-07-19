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

package org.eclipse.hudson.rest.api.build;

import javax.inject.Inject;

import org.eclipse.hudson.rest.api.internal.ConverterSupport;
import org.eclipse.hudson.rest.api.user.UserConverter;

import org.eclipse.hudson.rest.model.build.ChangeEntryDTO;
import org.eclipse.hudson.rest.model.build.ChangeFileDTO;
import org.eclipse.hudson.rest.model.build.ChangeTypeDTO;
import org.eclipse.hudson.rest.model.build.ChangesDTO;
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
