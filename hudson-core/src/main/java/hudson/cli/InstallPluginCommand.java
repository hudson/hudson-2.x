/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

package hudson.cli;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Hudson;
import hudson.model.UpdateSite;
import hudson.model.UpdateSite.Data;
import hudson.util.EditDistance;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Installs a plugin either from a file, an URL, or from update center.
 * 
 * @author Kohsuke Kawaguchi
 * @since 1.331
 */
@Extension
public class InstallPluginCommand extends CLICommand {
    public String getShortDescription() {
        return "Installs a plugin either from a file, an URL, or from update center";
    }

    @Argument(metaVar="SOURCE",required=true,usage="If this points to a local file, that file will be installed. " +
            "If this is an URL, Hudson downloads the URL and installs that as a plugin." +
            "Otherwise the name is assumed to be the short name of the plugin in the existing update center (like \"findbugs\")," +
            "and the plugin will be installed from the update center")
    public List<String> sources = new ArrayList<String>();

    @Option(name="-name",usage="If specified, the plugin will be installed as this short name (whereas normally the name is inferred from the source name automatically.)")
    public String name;

    @Option(name="-restart",usage="Restart Hudson upon successful installation")
    public boolean restart;

    protected int run() throws Exception {
        Hudson h = Hudson.getInstance();
        h.checkPermission(Hudson.ADMINISTER);

        for (String source : sources) {
            // is this a file?
            FilePath f = new FilePath(channel, source);
            if (f.exists()) {
                stdout.println(Messages.InstallPluginCommand_InstallingPluginFromLocalFile(f));
                if (name==null)
                    name = f.getBaseName();
                f.copyTo(getTargetFile());
                continue;
            }

            // is this an URL?
            try {
                URL u = new URL(source);
                stdout.println(Messages.InstallPluginCommand_InstallingPluginFromUrl(u));
                if (name==null) {
                    name = u.getPath();
                    name = name.substring(name.indexOf('/')+1);
                    name = name.substring(name.indexOf('\\')+1);
                    int idx = name.lastIndexOf('.');
                    if (idx>0)  name = name.substring(0,idx);
                }
                getTargetFile().copyFrom(u);
                continue;
            } catch (MalformedURLException e) {
                // not an URL
            }

            // is this a plugin the update center?
            UpdateSite.Plugin p = h.getUpdateCenter().getPlugin(source);
            if (p!=null) {
                stdout.println(Messages.InstallPluginCommand_InstallingFromUpdateCenter(source));
                p.deploy().get();
                continue;
            }

            stdout.println(Messages.InstallPluginCommand_NotAValidSourceName(source));

            if (!source.contains(".") && !source.contains(":") && !source.contains("/") && !source.contains("\\")) {
                // looks like a short plugin name. Why did we fail to find it in the update center?
                if (h.getUpdateCenter().getSites().isEmpty()) {
                    stdout.println(Messages.InstallPluginCommand_NoUpdateCenterDefined());
                } else {
                    Set<String> candidates = new HashSet<String>();
                    for (UpdateSite s : h.getUpdateCenter().getSites()) {
                        Data dt = s.getData();
                        if (dt==null) {
                            stdout.println(Messages.InstallPluginCommand_NoUpdateDataRetrieved(s.getUrl()));
                        } else {
                            candidates.addAll(dt.plugins.keySet());
                        }
                    }
                    stdout.println(Messages.InstallPluginCommand_DidYouMean(source,EditDistance.findNearest(source,candidates)));
                }
            }

            return 1;
        }

        if (restart)
            h.restart();
        return 0; // all success
    }

    private FilePath getTargetFile() {
        return new FilePath(new File(Hudson.getInstance().getPluginManager().rootDir,name+".hpi"));
    }
}
