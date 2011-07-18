/*
 * The MIT License
 *
 * Copyright 2011 Sun Microsystems, Inc., Kohsuke Kawaguchi
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
package hudson.util;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Immutable representation of a dot or '-'-separated digits (such as "1.0.1" or "1.0-52").
 *
 * {@link VersionNumber}s are {@link Comparable}.
 *
 * <h2>Special tokens</h2>
 * <p>
 * We allow a component to be not just a number, but also "ea", "ea1", "ea2".
 * "ea" is treated as "ea0", and eaN &lt; M for any M > 0.
 *
 * <p>
 * '*' is also allowed as a component, and '*' > M for any M > 0.
 *
 * <p>
 * 'SNAPSHOT' is also allowed as a component, and "N.SNAPSHOT" is interpreted as "N-1.*"
 *
 * <pre>
 * 2.0.* > 2.0.1 > 2.0.1-SNAPSHOT > 2.0.0.99 > 2.0.0 > 2.0.ea > 2.0
 * </pre>
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @since 1.139
 */
public class VersionNumber implements Comparable<VersionNumber> {
    private final int[] digits;

    /**
     * Parses a string like "1.0.2" into the version number.
     *
     * @throws IllegalArgumentException
     *      if the parsing fails.
     */
    public VersionNumber( String num ) {
        StringTokenizer tokens = new StringTokenizer(num,".-");
        digits = new int[tokens.countTokens()];
        if(digits.length<2)
            throw new IllegalArgumentException("Failed to parse "+num+" as version number");

        int i=0;
        while( tokens.hasMoreTokens() ) {
            String token = tokens.nextToken().toLowerCase(Locale.ENGLISH);
            if(token.equals("*")) {
                digits[i++] = 1000;
            } else
            if(token.startsWith("snapshot")) {
                digits[i-1]--;
                digits[i++] = 1000;
                break;
            } else
            if(token.startsWith("ea")) {
                if(token.length()==2)
                    digits[i++] = -1000;    // just "ea"
                else
                    digits[i++] = -1000 + Integer.parseInt(token.substring(2)); // "eaNNN"
            } else {
                digits[i++] = Integer.parseInt(token);
            }
        }
    }

    public int digit(int idx) {
        return digits[idx];
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for( int i=0; i<digits.length; i++ ) {
            if(i!=0)    buf.append('.');
            buf.append( Integer.toString(digits[i]) );
        }
        return buf.toString();
    }

    public boolean isOlderThan( VersionNumber rhs ) {
        return compareTo(rhs)<0;
    }

    public boolean isNewerThan( VersionNumber rhs ) {
        return compareTo(rhs)>0;
    }


    @Override
    public boolean equals( Object o ) {
        if (!(o instanceof VersionNumber))  return false;
        return compareTo((VersionNumber)o)==0;
    }

    @Override
    public int hashCode() {
        int x=0;
        for (int i : digits)
            x = (x << 1) | i;
        return x;
    }

    public int compareTo(VersionNumber rhs) {
        for( int i=0; ; i++ ) {
            if( i==this.digits.length && i==rhs.digits.length )
                return 0;   // equals
            if( i==this.digits.length )
                return -1;  // rhs is larger
            if( i==rhs.digits.length )
                return 1;

            int r = this.digits[i] - rhs.digits[i];
            if(r!=0)    return r;
        }
    }
}
