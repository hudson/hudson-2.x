/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.utils.common;

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
