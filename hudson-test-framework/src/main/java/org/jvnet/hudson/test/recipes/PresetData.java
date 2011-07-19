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

package org.jvnet.hudson.test.recipes;

import org.jvnet.hudson.test.HudsonTestCase;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.Locale;

/**
 * Runs a test case with one of the preset HUDSON_HOME data set.
 *
 * @author Kohsuke Kawaguchi
 * @see LocalData
 */
@Documented
@Recipe(PresetData.RunnerImpl.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface PresetData {
    /**
     * One of the preset data to choose from.
     */
    DataSet value();

    public enum DataSet {
        /**
         * Secured Hudson that has no anonymous read access.
         * Any logged in user can do anything.
         */
        NO_ANONYMOUS_READACCESS,
        /**
         * Secured Hudson where anonymous user is read-only,
         * and any logged in user has a full access.
         */
        ANONYMOUS_READONLY,
    }

    public class RunnerImpl extends Recipe.Runner<PresetData> {
        public void setup(HudsonTestCase testCase, PresetData recipe) {
            testCase.withPresetData(recipe.value().name().toLowerCase(Locale.ENGLISH).replace('_','-'));
        }
    }
}
