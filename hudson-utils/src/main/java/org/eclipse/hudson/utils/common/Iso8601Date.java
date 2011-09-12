/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.utils.common;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper for working with <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601<a/> dates.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class Iso8601Date
{
    public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    //public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";
    //public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    public static final FastDateFormat FORMAT = FastDateFormat.getInstance(PATTERN);

    private static SimpleDateFormat parser;

    public static synchronized Date parse(final String value) throws ParseException {
        if (parser == null) {
            parser = new SimpleDateFormat(PATTERN);
        }
        return parser.parse(value);
    }

    public static String format(final Date date) {
        return FORMAT.format(date);
    }
}
