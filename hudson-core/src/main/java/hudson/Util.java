/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi, Winston Prakash
 *     
 *
 *******************************************************************************/ 

package hudson;

import hudson.model.TaskListener;
import hudson.model.Hudson;

import hudson.util.IOException2;
import hudson.util.QuotedStringTokenizer;
import hudson.util.VariableResolver;
import hudson.Proc.LocalProc;
import hudson.util.jna.NativeAccessException;

import hudson.util.jna.NativeFunction;
import hudson.util.jna.NativeUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.io.IOUtils;

import org.kohsuke.stapler.Stapler;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.Charset;

/**
 * Various utility methods that don't have more proper home.
 *
 * @author Kohsuke Kawaguchi
 */
public class Util {

    // Constant number of milliseconds in various time units.
    private static final long ONE_SECOND_MS = 1000;
    private static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
    private static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;
    private static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
    private static final long ONE_MONTH_MS = 30 * ONE_DAY_MS;
    private static final long ONE_YEAR_MS = 365 * ONE_DAY_MS;

    /**
     * Creates a filtered sublist.
     * @since 1.176
     */
    public static <T> List<T> filter( Iterable<?> base, Class<T> type ) {
        List<T> r = new ArrayList<T>();
        for (Object i : base) {
            if(type.isInstance(i))
                r.add(type.cast(i));
        }
        return r;
    }

    /**
     * Creates a filtered sublist.
     */
    public static <T> List<T> filter( List<?> base, Class<T> type ) {
        return filter((Iterable)base,type);
    }

    /**
     * Pattern for capturing variables. Either $xyz or ${xyz}, while ignoring "$$"
      */
    private static final Pattern VARIABLE = Pattern.compile("\\$([A-Za-z0-9_\\.]+|\\{[A-Za-z0-9_\\.]+\\}|\\$)");

    /**
     * Replaces the occurrence of '$key' by <tt>properties.get('key')</tt>.
     *
     * <p>
     * Unlike shell, undefined variables are left as-is (this behavior is the same as Ant.)
     *
     */
    public static String replaceMacro(String s, Map<String,String> properties) {
        return replaceMacro(s,new VariableResolver.ByMap<String>(properties));
    }
    
    /**
     * Replaces the occurrence of '$key' by <tt>resolver.get('key')</tt>.
     *
     * <p>
     * Unlike shell, undefined variables are left as-is (this behavior is the same as Ant.)
     */
    public static String replaceMacro(String s, VariableResolver<String> resolver) {
    	if (s == null) {
    		return null;
    	}
    	
        int idx=0;
        while(true) {
            Matcher m = VARIABLE.matcher(s);
            if(!m.find(idx))   return s;

            String key = m.group().substring(1);

            // escape the dollar sign or get the key to resolve
            String value;
            if(key.charAt(0)=='$') {
               value = "$";
            } else {
               if(key.charAt(0)=='{')  key = key.substring(1,key.length()-1);
               value = resolver.resolve(key);
            }

            if(value==null)
                idx = m.end(); // skip this
            else {
                s = s.substring(0,m.start())+value+s.substring(m.end());
                idx = m.start() + value.length();
            }
        }
    }

    /**
     * Loads the contents of a file into a string.
     */
    public static String loadFile(File logfile) throws IOException {
        return loadFile(logfile, Charset.defaultCharset());
    }

    public static String loadFile(File logfile,Charset charset) throws IOException {
        if(!logfile.exists())
            return "";

        StringBuilder str = new StringBuilder((int)logfile.length());

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(logfile),charset));
        char[] buf = new char[1024];
        int len;
        while((len=r.read(buf,0,buf.length))>0)
           str.append(buf,0,len);
        r.close();

        return str.toString();
    }

    /**
     * Deletes the contents of the given directory (but not the directory itself)
     * recursively.
     *
     * @throws IOException
     *      if the operation fails.
     */
    public static void deleteContentsRecursive(File file) throws IOException {
        File[] files = file.listFiles();
        if(files==null)
            return;     // the directory didn't exist in the first place
        for (File child : files)
            deleteRecursive(child);
    }

    /**
     * Deletes this file (and does not take no for an answer).
     * @param f a file to delete
     * @throws IOException if it exists but could not be successfully deleted
     */
    public static void deleteFile(File f) throws IOException {
        if (!f.delete()) {
            if(!f.exists())
                // we are trying to delete a file that no longer exists, so this is not an error
                return;

            // perhaps this file is read-only?
            makeWritable(f);
            /*
             on Unix both the file and the directory that contains it has to be writable
             for a file deletion to be successful. (Confirmed on Solaris 9)

             $ ls -la
             total 6
             dr-xr-sr-x   2 hudson   hudson       512 Apr 18 14:41 .
             dr-xr-sr-x   3 hudson   hudson       512 Apr 17 19:36 ..
             -r--r--r--   1 hudson   hudson       469 Apr 17 19:36 manager.xml
             -rw-r--r--   1 hudson   hudson         0 Apr 18 14:41 x
             $ rm x
             rm: x not removed: Permission denied
             */

            makeWritable(f.getParentFile());

            if(!f.delete() && f.exists()) {
                // trouble-shooting.
                // see http://www.nabble.com/Sometimes-can%27t-delete-files-from-hudson.scm.SubversionSCM%24CheckOutTask.invoke%28%29-tt17333292.html
                // I suspect other processes putting files in this directory
                File[] files = f.listFiles();
                if(files!=null && files.length>0)
                    throw new IOException("Unable to delete " + f.getPath()+" - files in dir: "+Arrays.asList(files));
                throw new IOException("Unable to delete " + f.getPath());
            }
        }
    }

    /**
     * Makes the given file writable by any means possible.
     */
    @IgnoreJRERequirement
    private static void makeWritable(File f) {
        // try chmod. this becomes no-op if this is not Unix.
        try {
            Chmod chmod = new Chmod();
            chmod.setProject(new Project());
            chmod.setFile(f);
            chmod.setPerm("u+w");
            chmod.execute();
        } catch (BuildException e) {
            LOGGER.log(Level.INFO, "Failed to chmod " + f, e);
        }

        // also try JDK6-way of doing it.
        try {
            f.setWritable(true);
        } catch (NoSuchMethodError e) {
            // not JDK6
        }

        try {
            NativeUtils.getInstance().makeFileWritable(f);
        } catch (NativeAccessException exc) {
            LOGGER.log(Level.FINE, "Failed to chmod(2) " + f, exc);
        }

    }

    public static void deleteRecursive(File dir) throws IOException {
        if(!isSymlink(dir))
            deleteContentsRecursive(dir);
        deleteFile(dir);
    }
 
    /**
     * Checks if the given file represents a symlink.
     */
    public static boolean isSymlink(File file) throws IOException {
        String name = file.getName();
        if (name.equals(".") || name.equals(".."))
            return false;

        File fileInCanonicalParent;
        File parentDir = file.getParentFile();
        if ( parentDir == null ) {
            fileInCanonicalParent = file;
        } else {
            fileInCanonicalParent = new File( parentDir.getCanonicalPath(), name );
        }
        return !fileInCanonicalParent.getCanonicalFile().equals( fileInCanonicalParent.getAbsoluteFile() );
    }

    /**
     * Creates a new temporary directory.
     */
    public static File createTempDir() throws IOException {
        File tmp = File.createTempFile("hudson", "tmp");
        if(!tmp.delete())
            throw new IOException("Failed to delete "+tmp);
        if(!tmp.mkdirs())
            throw new IOException("Failed to create a new directory "+tmp);
        return tmp;
    }

    private static final Pattern errorCodeParser = Pattern.compile(".*CreateProcess.*error=([0-9]+).*");

    /**
     * On Windows, error messages for IOException aren't very helpful.
     * This method generates additional user-friendly error message to the listener
     */
    public static void displayIOException( IOException e, TaskListener listener ) {
        String msg = getWin32ErrorMessage(e);
        if(msg!=null)
            listener.getLogger().println(msg);
    }

    public static String getWin32ErrorMessage(IOException e) {
        return getWin32ErrorMessage((Throwable)e);
    }

    /**
     * Extracts the Win32 error message from {@link Throwable} if possible.
     *
     * @return
     *      null if there seems to be no error code or if the platform is not Win32.
     */
    public static String getWin32ErrorMessage(Throwable e) {
        String msg = e.getMessage();
        if(msg!=null) {
            Matcher m = errorCodeParser.matcher(msg);
            if(m.matches()) {
                try {
                    ResourceBundle rb = ResourceBundle.getBundle("/hudson/win32errors");
                    return rb.getString("error"+m.group(1));
                } catch (Exception _) {
                    // silently recover from resource related failures
                }
            }
        }

        if(e.getCause()!=null)
            return getWin32ErrorMessage(e.getCause());
        return null; // no message
    }

    /**
     * Gets a human readable message for the given Win32 error code.
     *
     * @return
     *      null if no such message is available.
     */
    public static String getWin32ErrorMessage(int n) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("/hudson/win32errors");
            return rb.getString("error"+n);
        } catch (MissingResourceException e) {
            LOGGER.log(Level.WARNING,"Failed to find resource bundle",e);
            return null;
        }
    }

    /**
     * Guesses the current host name.
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    public static void copyStream(InputStream in,OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while((len=in.read(buf))>0)
            out.write(buf,0,len);
    }

    public static void copyStream(Reader in, Writer out) throws IOException {
        char[] buf = new char[8192];
        int len;
        while((len=in.read(buf))>0)
            out.write(buf,0,len);
    }

    public static void copyStreamAndClose(InputStream in,OutputStream out) throws IOException {
        try {
            copyStream(in,out);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    public static void copyStreamAndClose(Reader in,Writer out) throws IOException {
        try {
            copyStream(in,out);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Tokenizes the text separated by delimiters.
     *
     * <p>
     * In 1.210, this method was changed to handle quotes like Unix shell does.
     * Before that, this method just used {@link StringTokenizer}.
     *
     * @since 1.145
     * @see QuotedStringTokenizer
     */
    public static String[] tokenize(String s,String delimiter) {
        return QuotedStringTokenizer.tokenize(s,delimiter);
    }

    public static String[] tokenize(String s) {
        return tokenize(s," \t\n\r\f");
    }

    /**
     * Converts the map format of the environment variables to the K=V format in the array.
     */
    public static String[] mapToEnv(Map<String,String> m) {
        String[] r = new String[m.size()];
        int idx=0;

        for (final Map.Entry<String,String> e : m.entrySet()) {
            r[idx++] = e.getKey() + '=' + e.getValue();
        }
        return r;
    }

    public static int min(int x, int... values) {
        for (int i : values) {
            if(i<x)
                x=i;
        }
        return x;
    }

    public static String nullify(String v) {
        if(v!=null && v.length()==0)    v=null;
        return v;
    }

    public static String removeTrailingSlash(String s) {
        if(s.endsWith("/")) return s.substring(0,s.length()-1);
        else                return s;
    }

    /**
     * Write-only buffer.
     */
    private static final byte[] garbage = new byte[8192];

    /**
     * Computes MD5 digest of the given input stream.
     *
     * @param source
     *      The stream will be closed by this method at the end of this method.
     * @return
     *      32-char wide string
     */
    public static String getDigestOf(InputStream source) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            DigestInputStream in =new DigestInputStream(source,md5);
            try {
                while(in.read(garbage)>0)
                    ; // simply discard the input
            } finally {
                in.close();
            }
            return toHexString(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException2("MD5 not installed",e);    // impossible
        }
    }

    public static String getDigestOf(String text) {
        try {
            return getDigestOf(new ByteArrayInputStream(text.getBytes("UTF-8")));
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Converts a string into 128-bit AES key.
     * @since 1.308
     */
    public static SecretKey toAes128Key(String s) {
        try {
            // turn secretKey into 256 bit hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(s.getBytes("UTF-8"));

            // Due to the stupid US export restriction JDK only ships 128bit version.
            return new SecretKeySpec(digest.digest(),0,128/8, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static String toHexString(byte[] data, int start, int len) {
        StringBuilder buf = new StringBuilder();
        for( int i=0; i<len; i++ ) {
            int b = data[start+i]&0xFF;
            if(b<16)    buf.append('0');
            buf.append(Integer.toHexString(b));
        }
        return buf.toString();
    }

    public static String toHexString(byte[] bytes) {
        return toHexString(bytes,0,bytes.length);
    }

    public static byte[] fromHexString(String data) {
        byte[] r = new byte[data.length() / 2];
        for (int i = 0; i < data.length(); i += 2)
            r[i / 2] = (byte) Integer.parseInt(data.substring(i, i + 2), 16);
        return r;
    }

    /**
     * Returns a human readable text of the time duration, for example "3 minutes 40 seconds".
     * This version should be used for representing a duration of some activity (like build)
     *
     * @param duration
     *      number of milliseconds.
     */
    public static String getTimeSpanString(long duration) {
        // Break the duration up in to units.
        long years = duration / ONE_YEAR_MS;
        duration %= ONE_YEAR_MS;
        long months = duration / ONE_MONTH_MS;
        duration %= ONE_MONTH_MS;
        long days = duration / ONE_DAY_MS;
        duration %= ONE_DAY_MS;
        long hours = duration / ONE_HOUR_MS;
        duration %= ONE_HOUR_MS;
        long minutes = duration / ONE_MINUTE_MS;
        duration %= ONE_MINUTE_MS;
        long seconds = duration / ONE_SECOND_MS;
        duration %= ONE_SECOND_MS;
        long millisecs = duration;

        if (years > 0)
            return makeTimeSpanString(years, Messages.Util_year(years), months, Messages.Util_month(months));
        else if (months > 0)
            return makeTimeSpanString(months, Messages.Util_month(months), days, Messages.Util_day(days));
        else if (days > 0)
            return makeTimeSpanString(days, Messages.Util_day(days), hours, Messages.Util_hour(hours));
        else if (hours > 0)
            return makeTimeSpanString(hours, Messages.Util_hour(hours), minutes, Messages.Util_minute(minutes));
        else if (minutes > 0)
            return makeTimeSpanString(minutes, Messages.Util_minute(minutes), seconds, Messages.Util_second(seconds));
        else if (seconds >= 10)
            return Messages.Util_second(seconds);
        else if (seconds >= 1)
            return Messages.Util_second(seconds+(float)(millisecs/100)/10); // render "1.2 sec"
        else if(millisecs>=100)
            return Messages.Util_second((float)(millisecs/10)/100); // render "0.12 sec".
        else
            return Messages.Util_millisecond(millisecs);
    }


    /**
     * Create a string representation of a time duration.  If the quantity of
     * the most significant unit is big (>=10), then we use only that most
     * significant unit in the string representation. If the quantity of the
     * most significant unit is small (a single-digit value), then we also
     * use a secondary, smaller unit for increased precision.
     * So 13 minutes and 43 seconds returns just "13 minutes", but 3 minutes
     * and 43 seconds is "3 minutes 43 seconds".
     */
    private static String makeTimeSpanString(long bigUnit,
                                             String bigLabel,
                                             long smallUnit,
                                             String smallLabel) {
        String text = bigLabel;
        if (bigUnit < 10)
            text += ' ' + smallLabel;
        return text;
    }


    /**
     * Get a human readable string representing strings like "xxx days ago",
     * which should be used to point to the occurrence of an event in the past.
     */
    public static String getPastTimeString(long duration) {
        return Messages.Util_pastTime(getTimeSpanString(duration));
    }


    /**
     * Combines number and unit, with a plural suffix if needed.
     * 
     * @deprecated 
     *   Use individual localization methods instead. 
     *   See {@link Messages#Util_year(Object)} for an example.
     *   Deprecated since 2009-06-24, remove method after 2009-12-24.
     */
    public static String combine(long n, String suffix) {
        String s = Long.toString(n)+' '+suffix;
        if(n!=1)
        	// Just adding an 's' won't work in most natural languages, even English has exception to the rule (e.g. copy/copies).
            s += "s";
        return s;
    }

    /**
     * Create a sub-list by only picking up instances of the specified type.
     */
    public static <T> List<T> createSubList( Collection<?> source, Class<T> type ) {
        List<T> r = new ArrayList<T>();
        for (Object item : source) {
            if(type.isInstance(item))
                r.add(type.cast(item));
        }
        return r;
    }

    /**
     * Escapes non-ASCII characters in URL.
     *
     * <p>
     * Note that this methods only escapes non-ASCII but leaves other URL-unsafe characters,
     * such as '#'.
     * {@link #rawEncode(String)} should generally be used instead, though be careful to pass only
     * a single path component to that method (it will encode /, but this method does not).
     */
    public static String encode(String s) {
        try {
            boolean escaped = false;

            StringBuilder out = new StringBuilder(s.length());

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(buf,"UTF-8");

            for (int i = 0; i < s.length(); i++) {
                int c = (int) s.charAt(i);
                if (c<128 && c!=' ') {
                    out.append((char) c);
                } else {
                    // 1 char -> UTF8
                    w.write(c);
                    w.flush();
                    for (byte b : buf.toByteArray()) {
                        out.append('%');
                        out.append(toDigit((b >> 4) & 0xF));
                        out.append(toDigit(b & 0xF));
                    }
                    buf.reset();
                    escaped = true;
                }
            }

            return escaped ? out.toString() : s;
        } catch (IOException e) {
            throw new Error(e); // impossible
        }
    }

    private static final boolean[] uriMap = new boolean[123];
    static {
        String raw =
    "!  $ &'()*+,-. 0123456789   =  @ABCDEFGHIJKLMNOPQRSTUVWXYZ    _ abcdefghijklmnopqrstuvwxyz";
  //  "# %         /          :;< >?                           [\]^ `                          {|}~
  //  ^--so these are encoded
        int i;
        // Encode control chars and space
        for (i = 0; i < 33; i++) uriMap[i] = true;
        for (int j = 0; j < raw.length(); i++, j++)
            uriMap[i] = (raw.charAt(j) == ' ');
        // If we add encodeQuery() just add a 2nd map to encode &+=
        // queryMap[38] = queryMap[43] = queryMap[61] = true;
    }

    /**
     * Encode a single path component for use in an HTTP URL.
     * Escapes all non-ASCII, general unsafe (space and "#%<>[\]^`{|}~)
     * and HTTP special characters (/;:?) as specified in RFC1738.
     * (so alphanumeric and !@$&*()-_=+',. are not encoded)
     * Note that slash(/) is encoded, so the given string should be a
     * single path component used in constructing a URL.
     */
    public static String rawEncode(String s) {
        boolean escaped = false;
        StringBuilder out = null;
        CharsetEncoder enc = null;
        CharBuffer buf = null;
        char c;
        for (int i = 0, m = s.length(); i < m; i++) {
            c = s.charAt(i);
            if (c > 122 || uriMap[c]) {
                if (!escaped) {
                    out = new StringBuilder(i + (m - i) * 3);
                    out.append(s.substring(0, i));
                    enc = Charset.forName("UTF-8").newEncoder();
                    buf = CharBuffer.allocate(1);
                    escaped = true;
                }
                // 1 char -> UTF8
                buf.put(0,c);
                buf.rewind();
                try {
                    ByteBuffer bytes = enc.encode(buf);
                    while (bytes.hasRemaining()) {
                        byte b = bytes.get();
                        out.append('%');
                        out.append(toDigit((b >> 4) & 0xF));
                        out.append(toDigit(b & 0xF));
                    }
                } catch (CharacterCodingException ex) { }
            } else if (escaped) {
                out.append(c);
            }
        }
        return escaped ? out.toString() : s;
    }

    private static char toDigit(int n) {
        return (char)(n < 10 ? '0' + n : 'A' + n - 10);
    }

    /**
     * Surrounds by a single-quote.
     */
    public static String singleQuote(String s) {
        return '\''+s+'\'';
    }

    /**
     * Escapes HTML unsafe characters like &lt;, &amp; to the respective character entities.
     */
    public static String escape(String text) {
        if (text==null)     return null;
        StringBuilder buf = new StringBuilder(text.length()+64);
        for( int i=0; i<text.length(); i++ ) {
            char ch = text.charAt(i);
            if(ch=='\n')
                buf.append("<br>");
            else
            if(ch=='<')
                buf.append("&lt;");
            else
            if(ch=='&')
                buf.append("&amp;");
            else
            if(ch=='"')
                buf.append("&quot;");
            else
            if(ch=='\'')
                buf.append("&#039;");
            else
            if(ch==' ') {
                // All spaces in a block of consecutive spaces are converted to
                // non-breaking space (&nbsp;) except for the last one.  This allows
                // significant whitespace to be retained without prohibiting wrapping.
                char nextCh = i+1 < text.length() ? text.charAt(i+1) : 0;
                buf.append(nextCh==' ' ? "&nbsp;" : " ");
            }
            else
                buf.append(ch);
        }
        return buf.toString();
    }

    public static String xmlEscape(String text) {
        StringBuilder buf = new StringBuilder(text.length()+64);
        for( int i=0; i<text.length(); i++ ) {
            char ch = text.charAt(i);
            if(ch=='<')
                buf.append("&lt;");
            else
            if(ch=='&')
                buf.append("&amp;");
            else
                buf.append(ch);
        }
        return buf.toString();

    }

    /**
     * Methods acts as {@link #xmlEscape(String)} method with only difference that it also escapes
     * '\n', '\r' symbols
     *
     * @param text string to escape
     * @return escaped string.
     */
    public static String escapeString(String text) {
        StringBuilder buf = new StringBuilder(text.length() + 64);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);// '\n', '\12', and '\x0A'
            switch (ch) {
                case '\n':
                    buf.append('\\').append('n');
                    break;
                case '\r':
                    buf.append('\\').append('r');
                    break;
                case '<':
                    buf.append("&lt;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                default:
                    buf.append(ch);
            }
        }
        return buf.toString();
    }

    /**
     * Creates an empty file.
     */
    public static void touch(File file) throws IOException {
        new FileOutputStream(file).close();
    }

    /**
     * Copies a single file by using Ant.
     */
    public static void copyFile(File src, File dst) throws BuildException {
        Copy cp = new Copy();
        cp.setProject(new org.apache.tools.ant.Project());
        cp.setTofile(dst);
        cp.setFile(src);
        cp.setOverwrite(true);
        cp.execute();
    }

    /**
     * Convert null to "".
     */
    public static String fixNull(String s) {
        if(s==null)     return "";
        else            return s;
    }

    /**
     * Convert empty string to null.
     */
    public static String fixEmpty(String s) {
        if(s==null || s.length()==0)    return null;
        return s;
    }

    /**
     * Convert empty string to null, and trim whitespace.
     *
     * @since 1.154
     */
    public static String fixEmptyAndTrim(String s) {
        if(s==null)    return null;
        return fixEmpty(s.trim());
    }

    public static <T> List<T> fixNull(List<T> l) {
        return l!=null ? l : Collections.<T>emptyList();
    }

    public static <T> Set<T> fixNull(Set<T> l) {
        return l!=null ? l : Collections.<T>emptySet();
    }

    public static <T> Collection<T> fixNull(Collection<T> l) {
        return l!=null ? l : Collections.<T>emptySet();
    }

    public static <T> Iterable<T> fixNull(Iterable<T> l) {
        return l!=null ? l : Collections.<T>emptySet();
    }

    /**
     * Cuts all the leading path portion and get just the file name.
     */
    public static String getFileName(String filePath) {
        int idx = filePath.lastIndexOf('\\');
        if(idx>=0)
            return getFileName(filePath.substring(idx+1));
        idx = filePath.lastIndexOf('/');
        if(idx>=0)
            return getFileName(filePath.substring(idx+1));
        return filePath;
    }

    /**
     * Concatenate multiple strings by inserting a separator.
     */
    public static String join(Collection<?> strings, String separator) {
        StringBuilder buf = new StringBuilder();
        boolean first=true;
        for (Object s : strings) {
            if(first)   first=false;
            else        buf.append(separator);
            buf.append(s);
        }
        return buf.toString();
    }

    /**
     * Combines all the given collections into a single list.
     */
    public static <T> List<T> join(Collection<? extends T>... items) {
        int size = 0;
        for (Collection<? extends T> item : items)
            size += item.size();
        List<T> r = new ArrayList<T>(size);
        for (Collection<? extends T> item : items)
            r.addAll(item);
        return r;
    }

    /**
     * Creates Ant {@link FileSet} with the base dir and include pattern.
     *
     * <p>
     * The difference with this and using {@link FileSet#setIncludes(String)}
     * is that this method doesn't treat whitespace as a pattern separator,
     * which makes it impossible to use space in the file path.
     *
     * @param includes
     *      String like "foo/bar/*.xml" Multiple patterns can be separated
     *      by ',', and whitespace can surround ',' (so that you can write
     *      "abc, def" and "abc,def" to mean the same thing.
     * @param excludes
     *      Exclusion pattern. Follows the same format as the 'includes' parameter.
     *      Can be null.
     * @since 1.172
     */
    public static FileSet createFileSet(File baseDir, String includes, String excludes) {
        FileSet fs = new FileSet();
        fs.setDir(baseDir);
        fs.setProject(new Project());

        StringTokenizer tokens;

        tokens = new StringTokenizer(includes,",");
        while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            fs.createInclude().setName(token);
        }
        if(excludes!=null) {
            tokens = new StringTokenizer(excludes,",");
            while(tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim();
                fs.createExclude().setName(token);
            }
        }
        return fs;
    }

    public static FileSet createFileSet(File baseDir, String includes) {
        return createFileSet(baseDir,includes,null);
    }

    /**
     * Creates a symlink to baseDir+targetPath at baseDir+symlinkPath.
     * <p>
     * If there's a prior symlink at baseDir+symlinkPath, it will be overwritten.
     *
     * @param baseDir
     *      Base directory to resolve the 'symlinkPath' parameter.
     * @param targetPath
     *      The file that the symlink should point to.
     * @param symlinkPath
     *      Where to create a symlink in.
     */
    public static void createSymlink(File baseDir, String targetPath, String symlinkPath, TaskListener listener) throws InterruptedException{
        if(Functions.isWindows() || NO_SYMLINK)   return;

        try {
            String errmsg = "";
            // if a file or a directory exists here, delete it first.
            // try simple delete first (whether exists() or not, as it may be symlink pointing
            // to non-existent target), but fallback to "rm -rf" to delete non-empty dir.
            File symlinkFile = new File(baseDir, symlinkPath);
            if (!symlinkFile.delete() && symlinkFile.exists())
                // ignore a failure.
                new LocalProc(new String[]{"rm","-rf", symlinkPath},new String[0],listener.getLogger(), baseDir).join();

            boolean success = false;

            try {
                success = NativeUtils.getInstance().createSymlink(targetPath, baseDir);
            } catch (NativeAccessException ex) {
                errmsg = "Native function mod failed" + NativeUtils.getInstance().getLastUnixError();
            }
            if (!success) { // escape hatch, until we know that the above works well.
                success = new LocalProc(new String[]{
                            "ln", "-s", targetPath, symlinkPath},
                        new String[0], listener.getLogger(), baseDir).join() == 0;
            }
            if (!success) {
                listener.getLogger().println(String.format("ln -s %s %s failed: %s", targetPath, symlinkFile, errmsg));
            }
        } catch (IOException e) {
            PrintStream log = listener.getLogger();
            log.printf("ln %s %s failed\n",targetPath, new File(baseDir, symlinkPath));
            Util.displayIOException(e,listener);
            e.printStackTrace( log );
        }
    }
    
    /**
     * Run chmod natively if we can, otherwise fall back to Ant.
     */
    public static void chmod(File f, int mask) {
        if (Functions.isWindows()) {
            return; // noop
        }
        try {
            NativeUtils.getInstance().chmod(f, mask);
        } catch (NativeAccessException exc) {
            LOGGER.log(Level.WARNING, "Native function chmod failed ({0}). Using Ant''s chmod task instead.", NativeUtils.getInstance().getLastUnixError());
            _chmodAnt(f, mask);
        }
    }

    private static void _chmodAnt(File f, int mask) {
        Chmod chmodTask = new Chmod();
        chmodTask.setProject(new Project());
        chmodTask.setFile(f);
        chmodTask.setPerm(Integer.toOctalString(mask));
        chmodTask.execute();
    }

    /**
     * Resolves symlink, if the given file is a symlink. Otherwise return null.
     * <p>
     * If the resolution fails, report an error.
     *
     * @param listener
     *      If we rely on an external command to resolve symlink, this is it.
     *      (TODO: try readlink(1) available on some platforms)
     */
    public static String resolveSymlink(File link, TaskListener listener) {
        if (Functions.isWindows()) {
            return null;
        }


        try {
            return NativeUtils.getInstance().resolveSymlink(link);
        } catch (NativeAccessException exc) {
            listener.getLogger().print("Native function resolveSymlink failed " + NativeUtils.getInstance().getLastUnixError());
        }


        return null;
    }

    /**
     * Encodes the URL by RFC 2396.
     *
     * I thought there's another spec that refers to UTF-8 as the encoding,
     * but don't remember it right now.
     *
     * @since 1.204
     * @deprecated since 2008-05-13. This method is broken (see ISSUE#1666). It should probably
     * be removed but I'm not sure if it is considered part of the public API
     * that needs to be maintained for backwards compatibility.
     * Use {@link #encode(String)} instead. 
     */
    @Deprecated
    public static String encodeRFC2396(String url) {
        try {
            return new URI(null,url,null).toASCIIString();
        } catch (URISyntaxException e) {
            LOGGER.warning("Failed to encode "+url);    // could this ever happen?
            return url;
        }
    }

    /**
     * Wraps with the error icon and the CSS class to render error message.
     * @since 1.173
     */
    public static String wrapToErrorSpan(String s) {
        s = "<span class=error><img src='"+
            Stapler.getCurrentRequest().getContextPath()+ Hudson.RESOURCE_PATH+
            "/images/none.gif' height=16 width=1>"+s+"</span>";
        return s;
    }
    
    /**
     * Returns the parsed string if parsed successful; otherwise returns the default number.
     * If the string is null, empty or a ParseException is thrown then the defaultNumber
     * is returned.
     * @param numberStr string to parse
     * @param defaultNumber number to return if the string can not be parsed
     * @return returns the parsed string; otherwise the default number
     */
    public static Number tryParseNumber(String numberStr, Number defaultNumber) {
        if ((numberStr == null) || (numberStr.length() == 0)) {
            return defaultNumber;
        }
        try {
            return NumberFormat.getNumberInstance().parse(numberStr);
        } catch (ParseException e) {
            return defaultNumber;
        }
    }

    /**
     * Checks if the public method defined on the base type with the given arguments
     * are overridden in the given derived type.
     */
    public static boolean isOverridden(Class base, Class derived, String methodName, Class... types) {
        // the rewriteHudsonWar method isn't overridden.
        try {
            return !base.getMethod(methodName, types).equals(
                    derived.getMethod(methodName,types));
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a file name by changing its extension.
     *
     * @param ext
     *      For example, ".zip"
     */
    public static File changeExtension(File dst, String ext) {
        String p = dst.getPath();
        int pos = p.lastIndexOf('.');
        if (pos<0)  return new File(p+ext);
        else        return new File(p.substring(0,pos)+ext);
    }

    /**
     * Null-safe String intern method.
     */
    public static String intern(String s) {
        return s==null ? s : s.intern();
    }

    /**
     * Loads a key/value pair string as {@link Properties}
     * @since 1.392
     */
    @IgnoreJRERequirement
    public static Properties loadProperties(String properties) throws IOException {
        Properties p = new Properties();
        try {
            p.load(new StringReader(properties));
        } catch (NoSuchMethodError e) {
            // load(Reader) method is only available on JDK6.
            // this fall back version doesn't work correctly with non-ASCII characters,
            // but there's no other easy ways out it seems.
            p.load(new ByteArrayInputStream(properties.getBytes()));
        }
        return p;
    }

    public static final FastDateFormat XS_DATETIME_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'",new SimpleTimeZone(0,"GMT"));

    // Note: RFC822 dates must not be localized!
    public static final FastDateFormat RFC822_DATETIME_FORMATTER
            = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    /**
     * On Unix environment that cannot run "ln", set this to true.
     */
    public static boolean NO_SYMLINK = Boolean.getBoolean(Util.class.getName()+".noSymLink");

    public static boolean SYMLINK_ESCAPEHATCH = Boolean.getBoolean(Util.class.getName()+".symlinkEscapeHatch");
}
