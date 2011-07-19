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

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.hudson.rest.api.internal.ConverterSupport;

import org.eclipse.hudson.rest.model.build.BuildDTO;
import org.eclipse.hudson.rest.model.build.BuildResultDTO;
import org.eclipse.hudson.rest.model.build.BuildStateDTO;
import org.eclipse.hudson.service.SystemService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts a {@link hudson.model.AbstractBuild} into a {@link BuildDTO} object.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class BuildConverter
    extends ConverterSupport
{
    private final SystemService systemService;

    private final CauseConverter causex;

    @Inject
    public BuildConverter(final SystemService systemService, final CauseConverter causex) {
        this.systemService = checkNotNull(systemService);
        this.causex = checkNotNull(causex);
    }

    public BuildDTO convert(final AbstractBuild<?, ?> source) {
        assert source != null;

        log.trace("Converting: {}", source);

        BuildDTO target = new BuildDTO();
        target.setType(source.getClass().getName());
        target.setUrl(String.format("%s/%s", this.systemService.getUrl(), source.getUrl()));
        target.setId(source.getId());
        target.setNumber(source.getNumber());
        target.setDescription(source.getDescription());
        target.setProjectName(source.getProject().getFullName());
        target.setDuration(source.getDuration());
        target.setTimeStamp(source.getTimestamp().getTimeInMillis());

        if (source.getResult() != null) {
            target.setResult(convert(source.getResult()));
        }

        if (source.hasntStartedYet()) {
            target.setState(BuildStateDTO.NOT_STARTED);
        }
        else if (source.isBuilding()) {
            target.setState(BuildStateDTO.BUILDING);
        }
        else if (source.isLogUpdated()) {
            target.setState(BuildStateDTO.POST_PRODUCTION);
        }
        else {
            target.setState(BuildStateDTO.COMPLETED);
        }

        // This is really more of a set, though I don't know how to make XJC
        // generate a Set<String>
        List<String> culprits = target.getCulprits();
        for (User user : source.getCulprits()) {
            String uid = user.getId();
            if (!culprits.contains(uid)) {
                culprits.add(uid);
            }
        }

        // This is really more of a set, though I don't know how to make XJC
        // generate a Set<String>
        List<String> participants = target.getParticipants();
        for (ChangeLogSet.Entry entry : source.getChangeSet()) {
            String uid = entry.getAuthor().getId();
            if (!participants.contains(uid)) {
                participants.add(uid);
            }
        }

        target.setKept(source.isKeepLog());
        target.getCauses().addAll(causex.convert(source.getCauses()));

        // XXX: source.getChangeSet() is currently guaranteed non-null, else
        // the loop above will throw a NullPointerException
        target.setChangesAvailable(source.getChangeSet() != null && !source.getChangeSet().isEmptySet());
        target.setTestsAvailable(source.getTestResultAction() != null && source.getTestResultAction().getTotalCount() != 0);

        return target;
    }

    public BuildDTO convert(final Run<?, ?> source) {
        assert source != null;

        log.trace("Converting: {}", source);

        return convert((AbstractBuild<?, ?>) source);
    }

    public BuildResultDTO convert(final Result source) {
        assert source != null;

        log.trace("Converting: {}", source);

        return BuildResultDTO.valueOf(source.toString().toUpperCase());
    }
}
