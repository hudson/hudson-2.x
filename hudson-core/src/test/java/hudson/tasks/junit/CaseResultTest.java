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
 *    Jesse Glick.
 *      
 *
 *******************************************************************************/ 

package hudson.tasks.junit;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.TestCase;
import org.jvnet.localizer.LocaleProvider;

public class CaseResultTest extends TestCase {
    
    public CaseResultTest(String name) {
        super(name);
    }
    
    // @Bug(6824)
    public void testLocalizationOfStatus() throws Exception {
        LocaleProvider old = LocaleProvider.getProvider();
        try {
            final AtomicReference<Locale> locale = new AtomicReference<Locale>();
            LocaleProvider.setProvider(new LocaleProvider() {
                public @Override Locale get() {
                    return locale.get();
                }
            });
            locale.set(Locale.GERMANY);
            assertEquals("Erfolg", CaseResult.Status.PASSED.getMessage());
            locale.set(Locale.US);
            assertEquals("Passed", CaseResult.Status.PASSED.getMessage());
        } finally {
            LocaleProvider.setProvider(old);
        }
    }
    
}
