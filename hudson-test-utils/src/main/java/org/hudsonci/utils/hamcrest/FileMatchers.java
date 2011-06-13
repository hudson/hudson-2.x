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

package org.hudsonci.utils.hamcrest;

import java.io.File;
import java.io.IOException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Bulk of implementation copied freely from
 * http://www.time4tea.net/wiki/display/MAIN/Testing+Files+with+Hamcrest
 * <p>
 * Converted to pure Hamcrest
 *
 * @author time4tea technology ltd 2007
 *
 */
public class FileMatchers {

    public static Matcher<File> isDirectory() {
        return new TypeSafeMatcher<File>() {
            File fileTested;

            @Override
            public boolean matchesSafely(File item) {
                fileTested = item;
                return item.isDirectory();
            }

            public void describeTo(Description description) {
                description.appendText(" that ");
                description.appendValue(fileTested);
                description.appendText("is a directory");
            }
        };
    }

    public static Matcher<File> exists() {
        return new TypeSafeMatcher<File>() {
            File fileTested;

            @Override
            public boolean matchesSafely(File item) {
                fileTested = item;
                return item.exists();
            }

            public void describeTo(Description description) {
                description.appendText(" that file ");
                description.appendValue(fileTested);
                description.appendText(" exists");
            }

        };
    }

    public static Matcher<File> isFile() {
        return new TypeSafeMatcher<File>() {
            File fileTested;

            @Override
            public boolean matchesSafely(File item) {
                fileTested = item;
                return item.isFile();
            }

            public void describeTo(Description description) {
                description.appendText(" that ");
                description.appendValue(fileTested);
                description.appendText("is a file");
            }
        };
    }

    public static Matcher<File> readable() {
        return new TypeSafeMatcher<File>() {
            File fileTested;

            @Override
            public boolean matchesSafely(File item) {
                fileTested = item;
                return item.canRead();
            }

            public void describeTo(Description description) {
                description.appendText(" that file ");
                description.appendValue(fileTested);
                description.appendText("is readable");
            }
        };
    }

    public static Matcher<File> writable() {
        return new TypeSafeMatcher<File>() {
            File fileTested;

            @Override
            public boolean matchesSafely(File item) {
                fileTested = item;
                return item.canWrite();
            }

            public void describeTo(Description description) {
                description.appendText(" that file ");
                description.appendValue(fileTested);
                description.appendText("is writable");
            }
        };
    }

    // public static Matcher<File> sized(Long size) {
    // return sized(Matchers.equalTo(size));
    // }

    public static Matcher<File> sized(final Matcher<Long> size) {
        return new TypeSafeMatcher<File>() {
            File fileTested;
            long length;

            @Override
            public boolean matchesSafely(File item) {
                fileTested = item;
                length = item.length();
                return size.matches(length);
            }

            public void describeTo(Description description) {
                description.appendText(" that file ");
                description.appendValue(fileTested);
                description.appendText(" is sized ");
                description.appendDescriptionOf(size);
                description.appendText(", not " + length);
            }
        };
    }

    public static Matcher<File> named(final Matcher<String> name) {
        return new TypeSafeMatcher<File>() {
            File fileTested;

            @Override
            public boolean matchesSafely(File item) {
                fileTested = item;
                return name.matches(item.getName());
            }

            public void describeTo(Description description) {
                description.appendText(" that file ");
                description.appendValue(fileTested);
                description.appendText(" is named");
                description.appendDescriptionOf(name);
                description.appendText(" not ");
                description.appendValue(fileTested.getName());
            }
        };
    }

    public static Matcher<File> withCanonicalPath(final Matcher<String> path) {
        return new TypeSafeMatcher<File>() {
            @Override
            public boolean matchesSafely(File item) {
                try {
                    return path.matches(item.getCanonicalPath());
                } catch (IOException e) {
                    return false;
                }
            }

            public void describeTo(Description description) {
                description.appendText("with canonical path '");
                description.appendDescriptionOf(path);
                description.appendText("'");
            }
        };
    }

    public static Matcher<File> withAbsolutePath(final Matcher<String> path) {
        return new TypeSafeMatcher<File>() {
            // File fileTested;

            @Override
            public boolean matchesSafely(File item) {
                // fileTested = item;
                return path.matches(item.getAbsolutePath());
            }

            public void describeTo(Description description) {
                description.appendText("with absolute path '");
                description.appendDescriptionOf(path);
                description.appendText("'");
            }
        };
    }
}
