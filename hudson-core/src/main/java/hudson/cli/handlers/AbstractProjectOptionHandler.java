/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.cli.handlers;

import hudson.model.AbstractProject;
import hudson.model.Hudson;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.MetaInfServices;

/**
 * Refer to {@link AbstractProject} by its name.
 *
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
public class AbstractProjectOptionHandler extends OptionHandler<AbstractProject> {
    public AbstractProjectOptionHandler(CmdLineParser parser, OptionDef option, Setter<AbstractProject> setter) {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        Hudson h = Hudson.getInstance();
        String src = params.getParameter(0);

        AbstractProject s = h.getItemByFullName(src,AbstractProject.class);
        if (s==null)
            throw new CmdLineException(owner, "No such job '"+src+"' perhaps you meant "+ AbstractProject.findNearest(src)+"?");
        setter.addValue(s);
        return 1;
    }

    @Override
    public String getDefaultMetaVariable() {
        return "JOB";
    }
}
