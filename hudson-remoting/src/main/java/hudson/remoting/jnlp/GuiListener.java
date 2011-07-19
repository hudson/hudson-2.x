/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.remoting.jnlp;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import hudson.remoting.EngineListener;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 * {@link EngineListener} implementation that shows GUI.
 */
public final class GuiListener implements EngineListener {
    public final MainDialog frame;

    public GuiListener() {
        GUI.setUILookAndFeel();
        frame = new MainDialog();
        frame.setVisible(true);
    }

    public void status(final String msg) {
        status(msg,null);
    }

    public void status(final String msg, final Throwable t) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.status(msg);
                if(t!=null)
                    LOGGER.log(INFO, msg, t);
            }
        });
    }

    public void error(final Throwable t) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LOGGER.log(SEVERE, t.getMessage(), t);
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                JOptionPane.showMessageDialog(
                    frame,sw.toString(),"Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        });
    }

    public void onDisconnect() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // discard all the menu items that might have been added by the master.
                frame.resetMenuBar();
            }
        });
    }

    private static final Logger LOGGER = Logger.getLogger(GuiListener.class.getName());
}
