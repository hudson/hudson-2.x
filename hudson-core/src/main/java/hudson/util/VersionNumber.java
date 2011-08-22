/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation
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

package hudson.util;

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
        StringTokenizer tokens = new StringTokenizer(num,".-_");
        digits = new int[tokens.countTokens()];
        if(digits.length<2)
            throw new IllegalArgumentException("Failed to parse "+num+" as version number");

        int i=0;
        while( tokens.hasMoreTokens() ) {
            String token = tokens.nextToken().toLowerCase();
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
                int n =0;
                try {
                    n = Integer.parseInt(token);
                } catch (NumberFormatException e) {
                    // ignore
                }
                digits[i++] = n;
            }
        }
    }

    public int digit(int idx) {
        return digits[idx];
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
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
