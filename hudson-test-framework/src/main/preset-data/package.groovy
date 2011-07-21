/**************************************************************************
#
# Copyright (C) 2004-2009 Oracle Corporation
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#         Kohsuke Kawaguchi
#
#**************************************************************************/ 
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.Project;

def baseDir = project.basedir;

File targetDir = new File(baseDir,"target/classes");
targetDir.mkdirs();

File dataSetRoot = new File(baseDir,"src/main/preset-data");
dataSetRoot.eachDir { d ->
    if(d.name==".svn") return;
    // if(!new File(d,"config.xml").exists())  return;

    Zip zip = new Zip();
    zip.project = new Project();
    zip.destFile = new File(targetDir,d.name+".zip");
    zip.basedir = d;
    zip.execute();
}
