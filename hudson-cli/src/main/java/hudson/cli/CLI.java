/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.cli;

import java.io.IOException;
import java.net.URL;

/**
 * Exists for backward compatibility
 * @author Winston Prakash
 * @see org.eclipse.hudson.cli.CLI
 */
public class CLI extends org.eclipse.hudson.cli.CLI {

    public CLI(URL hudson) throws IOException, InterruptedException {
        super(hudson);
    }
}
