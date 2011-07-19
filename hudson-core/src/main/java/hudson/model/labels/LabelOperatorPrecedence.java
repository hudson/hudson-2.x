/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.model.labels;

/**
 * Precedence of the top most operator.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.372
 */
public enum LabelOperatorPrecedence {
    ATOM(null), NOT("!"), AND("&&"), OR("||"), IMPLIES("->"), IFF("<->");

    /**
     * String representation of this operator.
     */
    public final String str;

    LabelOperatorPrecedence(String str) {
        this.str = str;
    }
}
