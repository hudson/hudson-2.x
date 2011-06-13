/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc.
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
package hudson.cli;

import hudson.model.Hudson;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.util.IOUtils;
import java.io.File;
import java.io.IOException;
import org.kohsuke.args4j.Argument;

/**
 * Updates or creates a job by reading stdin as a configuration XML file.
 * 
 * @author Henrik Lynggaard Hansen
 */
@Extension
public class UpdateJobCommand extends CLICommand {

    @Override
    public String getShortDescription() {
        return "Updates and potentionally creates a job by reading stdin as a configuration XML file.";
    }
    
    @Argument(metaVar = "NAME", usage = "Name of the job to update", required = true)
    public String name;
    @Argument(metaVar = "CREATE", usage = "Create the job if needed", index = 1, required = true)
    public Boolean create;

    protected int run() throws Exception {
        Hudson h = Hudson.getInstance();

        TopLevelItem item = h.getItem(name);

        if (item == null && !create) {
            stderr.println("Job '" + name + "' does not exist and create is set to false");
            return -1;
        }

        if (item == null) {
            h.checkPermission(Item.CREATE);
            h.createProjectFromXML(name, stdin);
        } else {
            try {               
                h.checkPermission(Job.CONFIGURE);
                               
                File rootDirOfJob = new File(new File(h.getRootDir(), "jobs"), name);
                // place it as config.xml
                File configXml = Items.getConfigFile(rootDirOfJob).getFile();
                IOUtils.copy(stdin, configXml);
                
                item = h.reloadProjectFromDisk(configXml.getParentFile());                               
            } catch (IOException e) {
                throw e;
            }
        }
        return 0;
    }
}
