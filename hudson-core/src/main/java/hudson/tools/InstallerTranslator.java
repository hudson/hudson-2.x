/*******************************************************************************
 *
 * Copyright (c) 2009, Oracle Corporation
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

package hudson.tools;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;

/**
 * Actually runs installations.
 * @since 1.305
 */
@Extension
public class InstallerTranslator extends ToolLocationTranslator {

    private static final Map<Node,Map<ToolInstallation,Semaphore>> mutexByNode = new WeakHashMap<Node,Map<ToolInstallation,Semaphore>>();

    public String getToolHome(Node node, ToolInstallation tool, TaskListener log) throws IOException, InterruptedException {
        InstallSourceProperty isp = tool.getProperties().get(InstallSourceProperty.class);
        if (isp == null) {
            return null;
        }
        for (ToolInstaller installer : isp.installers) {
            if (installer.appliesTo(node)) {
                Map<ToolInstallation, Semaphore> mutexByTool = mutexByNode.get(node);
                if (mutexByTool == null) {
                    mutexByNode.put(node, mutexByTool = new WeakHashMap<ToolInstallation, Semaphore>());
                }
                Semaphore semaphore = mutexByTool.get(tool);
                if (semaphore == null) {
                    mutexByTool.put(tool, semaphore = new Semaphore(1));
                }
                semaphore.acquire();
                try {
                    return installer.performInstallation(tool, node, log).getRemote();
                } finally {
                    semaphore.release();
                }
            }
        }
        return null;
    }

}
