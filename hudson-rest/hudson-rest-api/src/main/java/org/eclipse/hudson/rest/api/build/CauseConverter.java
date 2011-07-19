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

import org.eclipse.hudson.rest.model.build.CauseDTO;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hudson.rest.api.internal.ConverterSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts {@link hudson.model.Cause} into {@link CauseDTO} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class CauseConverter
    extends ConverterSupport
{
    public CauseDTO convert(final hudson.model.Cause source) {
        checkNotNull(source);

        log.trace("Converting: {}", source);

        CauseDTO target = new CauseDTO();
        target.setType(source.getClass().getName());
        target.setDescription(source.getShortDescription());
        // TODO: Render description.jelly view of source as target's detail

        return target;
    }

    public List<CauseDTO> convert(final List<hudson.model.Cause> source) {
        checkNotNull(source);

        List<CauseDTO> target = new ArrayList<CauseDTO>(source.size());

        for (hudson.model.Cause cause : source) {
            target.add(convert(cause));
        }

        return target;
    }
}
