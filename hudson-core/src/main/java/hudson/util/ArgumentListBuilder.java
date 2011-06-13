/*
 * The MIT License
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 * Alan Harder, Yahoo! Inc.
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

import hudson.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.BitSet;
import java.util.Properties;
import java.util.Map.Entry;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Used to build up arguments for a process invocation.
 *
 * @author Kohsuke Kawaguchi
 */
public class ArgumentListBuilder implements Serializable {
    private final List<String> args = new ArrayList<String>();
    /**
     * Bit mask indicating arguments that shouldn't be echoed-back (e.g., password)
     */
    private BitSet mask = new BitSet();

    public ArgumentListBuilder() {
    }

    public ArgumentListBuilder(String... args) {
        add(args);
    }

    public ArgumentListBuilder add(Object a) {
        return add(a.toString(), false);
    }

    /**
     * @since 1.378
     */
    public ArgumentListBuilder add(Object a, boolean mask) {
        return add(a.toString(), mask);
    }

    public ArgumentListBuilder add(File f) {
        return add(f.getAbsolutePath(), false);
    }

    public ArgumentListBuilder add(String a) {
        return add(a,false);
    }

    /**
     * @since 1.378
     */
    public ArgumentListBuilder add(String a, boolean mask) {
        if(a!=null) {
            if(mask) {
                this.mask.set(args.size());
            }
            args.add(a);
        }
        return this;
    }
    
    public ArgumentListBuilder prepend(String... args) {
        // left-shift the mask
        BitSet nm = new BitSet(this.args.size()+args.length);
        for(int i=0; i<this.args.size(); i++)
            nm.set(i+args.length, mask.get(i));
        mask = nm;

        this.args.addAll(0, Arrays.asList(args));
        return this;
    }

    /**
     * Adds an argument by quoting it.
     * This is necessary only in a rare circumstance,
     * such as when adding argument for ssh and rsh.
     *
     * Normal process invocations don't need it, because each
     * argument is treated as its own string and never merged into one. 
     */
    public ArgumentListBuilder addQuoted(String a) {
        return add('"'+a+'"', false);
    }

    /**
     * @since 1.378
     */
    public ArgumentListBuilder addQuoted(String a, boolean mask) {
        return add('"'+a+'"', mask);
    }

    public ArgumentListBuilder add(String... args) {
        for (String arg : args) {
            add(arg);
        }
        return this;
    }

    /**
     * Decomposes the given token into multiple arguments by splitting via whitespace.
     */
    public ArgumentListBuilder addTokenized(String s) {
        if(s==null) return this;
        add(Util.tokenize(s));
        return this;
    }

    /**
     * @since 1.378
     */
    public ArgumentListBuilder addKeyValuePair(String prefix, String key, String value, boolean mask) {
        if(key==null) return this;
        add(((prefix==null)?"-D":prefix)+key+'='+value, mask);
        return this;
    }

    /**
     * Adds key value pairs as "-Dkey=value -Dkey=value ..."
     *
     * <tt>-D</tt> portion is configurable as the 'prefix' parameter.
     * @since 1.114
     */
    public ArgumentListBuilder addKeyValuePairs(String prefix, Map<String,String> props) {
        for (Entry<String,String> e : props.entrySet())
            addKeyValuePair(prefix, e.getKey(), e.getValue(), false);
        return this;
    }

    /**
     * Adds key value pairs as "-Dkey=value -Dkey=value ..." with masking.
     *
     * @param prefix
     *      Configures the -D portion of the example. Defaults to -D if null.
     * @param props
     *      The map of key/value pairs to add
     * @param propsToMask
     *      Set containing key names to mark as masked in the argument list. Key
     *      names that do not exist in the set will be added unmasked.
     * @since 1.378
     */
    public ArgumentListBuilder addKeyValuePairs(String prefix, Map<String,String> props, Set<String> propsToMask) {
        for (Entry<String,String> e : props.entrySet()) {
            addKeyValuePair(prefix, e.getKey(), e.getValue(), (propsToMask == null) ? false : propsToMask.contains(e.getKey()));
        }
        return this;
    }

    /**
     * Adds key value pairs as "-Dkey=value -Dkey=value ..." by parsing a given string using {@link Properties}.
     *
     * @param prefix
     *      The '-D' portion of the example. Defaults to -D if null.
     * @param properties
     *      The persisted form of {@link Properties}. For example, "abc=def\nghi=jkl". Can be null, in which
     *      case this method becomes no-op.
     * @param vr
     *      {@link VariableResolver} to be performed on the values.
     * @since 1.262
     */
    public ArgumentListBuilder addKeyValuePairsFromPropertyString(String prefix, String properties, VariableResolver vr) throws IOException {
        if(properties==null)    return this;

        for (Entry<Object,Object> entry : Util.loadProperties(properties).entrySet()) {
            addKeyValuePair(prefix, (String)entry.getKey(), Util.replaceMacro(entry.getValue().toString(),vr), false);
        }
        return this;
    }

    /**
     * Adds key value pairs as "-Dkey=value -Dkey=value ..." by parsing a given string using {@link Properties} with masking.
     *
     * @param prefix
     *      The '-D' portion of the example. Defaults to -D if null.
     * @param properties
     *      The persisted form of {@link Properties}. For example, "abc=def\nghi=jkl". Can be null, in which
     *      case this method becomes no-op.
     * @param vr
     *      {@link VariableResolver} to be performed on the values.
     * @param propsToMask
     *      Set containing key names to mark as masked in the argument list. Key
     *      names that do not exist in the set will be added unmasked.
     * @since 1.378
     */
    public ArgumentListBuilder addKeyValuePairsFromPropertyString(String prefix, String properties, VariableResolver vr, Set<String> propsToMask) throws IOException {
        if(properties==null)    return this;

        for (Entry<Object,Object> entry : Util.loadProperties(properties).entrySet()) {
            addKeyValuePair(prefix, (String)entry.getKey(), Util.replaceMacro(entry.getValue().toString(),vr), (propsToMask == null) ? false : propsToMask.contains((String)entry.getKey()));
        }
        return this;
    }

    @Override
    public String toString() {
        return args.toString();
    }

    public String[] toCommandArray() {
        return args.toArray(new String[args.size()]);
    }
    
    @Override
    public ArgumentListBuilder clone() {
        ArgumentListBuilder r = new ArgumentListBuilder();
        r.args.addAll(this.args);
        r.mask = (BitSet) this.mask.clone();
        return r;
    }

    /**
     * Re-initializes the arguments list.
     */
    public void clear() {
        args.clear();
        mask.clear();
    }

    public List<String> toList() {
        return args;
    }

    /**
     * Just adds quotes around args containing spaces, but no other special characters,
     * so this method should generally be used only for informational/logging purposes.
     */
    public String toStringWithQuote() {
        StringBuilder buf = new StringBuilder();
        for (String arg : args) {
            if(buf.length()>0)  buf.append(' ');

            if(arg.indexOf(' ')>=0 || arg.length()==0)
                buf.append('"').append(arg).append('"');
            else
                buf.append(arg);
        }
        return buf.toString();
    }

    /**
     * Wrap command in a CMD.EXE call so we can return the exit code (ERRORLEVEL).
     * This method takes care of escaping special characters in the command, which
     * is needed since the command is now passed as a string to the CMD.EXE shell.
     * This is done as follows:
     * Wrap arguments in double quotes if they contain any of:
     *   space *?,;^&<>|" or % followed by a letter.
     * <br/> When testing from command prompt, these characters also need to be
     * prepended with a ^ character: ^&<>|  -- however, invoking cmd.exe from
     * Hudson does not seem to require this extra escaping so it is not added by
     * this method.
     * <br/> A " is prepended with another " character.  Note: Windows has issues
     * escaping some combinations of quotes and spaces.  Quotes should be avoided.
     * <br/> A % followed by a letter has that letter wrapped in double quotes,
     * to avoid possible variable expansion.  ie, %foo% becomes "%"f"oo%".
     * The second % does not need special handling because it is not followed
     * by a letter. <br/>
     * Example: "-Dfoo=*abc?def;ghi^jkl&mno<pqr>stu|vwx""yz%"e"nd"
     * @return new ArgumentListBuilder that runs given command through cmd.exe /C
     * @since 1.386
     */
    public ArgumentListBuilder toWindowsCommand() {
        StringBuilder quotedArgs = new StringBuilder();
        boolean quoted, percent;
        for (String arg : args) {
            quoted = percent = false;
            for (int i = 0; i < arg.length(); i++) {
                char c = arg.charAt(i);
                if (!quoted && (c == ' ' || c == '*' || c == '?' || c == ',' || c == ';')) {
                    quoted = startQuoting(quotedArgs, arg, i);
                }
                else if (c == '^' || c == '&' || c == '<' || c == '>' || c == '|') {
                    if (!quoted) quoted = startQuoting(quotedArgs, arg, i);
                    // quotedArgs.append('^'); See note in javadoc above
                }
                else if (c == '"') {
                    if (!quoted) quoted = startQuoting(quotedArgs, arg, i);
                    quotedArgs.append('"');
                }
                else if (percent && ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
                    if (!quoted) quoted = startQuoting(quotedArgs, arg, i);
                    quotedArgs.append('"').append(c);
                    c = '"';
                }
                percent = (c == '%');
                if (quoted) quotedArgs.append(c);
            }
            if (quoted) quotedArgs.append('"'); else quotedArgs.append(arg);
            quotedArgs.append(' ');
        }
        // (comment copied from old code in hudson.tasks.Ant)
        // on Windows, executing batch file can't return the correct error code,
        // so we need to wrap it into cmd.exe.
        // double %% is needed because we want ERRORLEVEL to be expanded after
        // batch file executed, not before. This alone shows how broken Windows is...
        quotedArgs.append("&& exit %%ERRORLEVEL%%");
        return new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(quotedArgs.toString());
    }

    private static boolean startQuoting(StringBuilder buf, String arg, int atIndex) {
        buf.append('"').append(arg.substring(0, atIndex));
        return true;
    }

    /**
     * Returns true if there are any masked arguments.
     * @return true if there are any masked arguments; false otherwise
     */
    public boolean hasMaskedArguments() {
        return mask.length()>0;
    }

    /**
     * Returns an array of booleans where the masked arguments are marked as true
     * @return an array of booleans.
     */
    public boolean[] toMaskArray() {
        boolean[] mask = new boolean[args.size()];
        for( int i=0; i<mask.length; i++)
            mask[i] = this.mask.get(i);
        return mask;
    }

    /**
     * Add a masked argument
     * @param string the argument
     */
    public void addMasked(String string) {
        add(string, true);
    }

    private static final long serialVersionUID = 1L;
}
