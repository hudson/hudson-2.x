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

package org.eclipse.hudson.rest.api.project;

import javax.inject.Inject;


import org.eclipse.hudson.rest.api.internal.ConverterSupport;
import org.eclipse.hudson.rest.model.project.HealthDTO;
import hudson.model.HealthReport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts {@link HealthReport} to {@link HealthDTO} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class HealthConverter
    extends ConverterSupport
{
    @Inject
    HealthConverter() {
        super();
    }

    /**
     *
     * @param source the source to convert
     *            what to convert
     * @throws NullPointerException
     *             if source is null
     * @return a converted HealthReport
     */
    public HealthDTO convert(final HealthReport source) {
        checkNotNull(source);

        log.trace("Converting: {}", source);

        HealthDTO target = new HealthDTO();
        target.setScore(source.getScore());
        target.setDescription(source.getDescription());

        return target;
    }
}
