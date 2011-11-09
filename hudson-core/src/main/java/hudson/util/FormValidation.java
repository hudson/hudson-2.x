/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc.
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

import hudson.EnvVars;
import hudson.Functions;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.FilePath;
import static hudson.Util.fixEmpty;
import hudson.model.Hudson;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.Stapler;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * Represents the result of the form field validation.
 *
 * <p>
 * Use one of the factory methods to create an instance, then return it from your <tt>doCheckXyz</tt>
 * method. (Via {@link HttpResponse}, the returned object will render the result into {@link StaplerResponse}.)
 * This way of designing form field validation allows you to reuse {@code doCheckXyz()} methods
 * programmatically as well (by using {@link #kind}.
 *
 * <p>
 * For typical validation needs, this class offers a number of {@code validateXXX(...)} methods, such as
 * {@link #validateExecutable(String)}. {@link FilePath} also has a number of {@code validateXXX(...)} methods
 * that you may be able to reuse.
 *
 * <p>
 * Also see {@link CVSSCM.DescriptorImpl#doCheckCvsRoot(String)} as an example.
 *
 * <p>
 * This class extends {@link IOException} so that it can be thrown from a method. This allows one to reuse
 * the checking logic as a part of the real computation, such as:
 *
 * <pre>
 * String getAntVersion(File antHome) throws FormValidation {
 *    if(!antHome.isDirectory())
 *        throw FormValidation.error(antHome+" doesn't look like a home directory");
 *    ...
 *    return IOUtils.toString(new File(antHome,"version"));
 * }
 *
 * ...
 *
 * public FormValidation doCheckAntVersion(@QueryParameter String f) {
 *     try {
 *         return ok(getAntVersion(new File(f)));
 *     } catch (FormValidation f) {
 *         return f;
 *     }
 * }
 *
 * ...
 *
 * public void {@linkplain hudson.tasks.Builder#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener) perform}(...) {
 *     String version = getAntVersion(antHome);
 *     ...
 * }
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 * @since 1.294
 */
public abstract class FormValidation extends IOException implements HttpResponse {
    /**
     * Indicates the kind of result.
     */
    public enum Kind {
        /**
         * Form field value was OK and no problem was detected.
         */
        OK,
        /**
         * Form field value contained something suspicious. For some limited use cases
         * the value could be valid, but we suspect the user made a mistake.
         */
        WARNING,
        /**
         * Form field value contained a problem that should be corrected.
         */
        ERROR
    }

    /**
     * Sends out a string error message that indicates an error.
     *
     * @param message
     *      Human readable message to be sent. <tt>error(null)</tt>
     *      can be used as <tt>ok()</tt>.
     */
    public static FormValidation error(String message) {
        return errorWithMarkup(message==null?null: Util.escape(message));
    }

    public static FormValidation warning(String message) {
        return warningWithMarkup(message==null?null:Util.escape(message));
    }

    public static FormValidation ok(String message) {
        return okWithMarkup(message==null?null:Util.escape(message));
    }

    /**
     * Singleton instance that represents "OK".
     */
    private static final FormValidation OK = respond(Kind.OK,"<div/>");

    public static FormValidation ok() {
        return OK;
    }

    /**
     * Sends out a string error message that indicates an error,
     * by formatting it with {@link String#format(String, Object[])}
     */
    public static FormValidation error(String format, Object... args) {
        return error(String.format(format,args));
    }

    public static FormValidation warning(String format, Object... args) {
        return warning(String.format(format,args));
    }

    public static FormValidation ok(String format, Object... args) {
        return ok(String.format(format,args));
    }

    /**
     * Sends out a string error message, with optional "show details" link that expands to the full stack trace.
     *
     * <p>
     * Use this with caution, so that anonymous users do not gain too much insights into the state of the system,
     * as error stack trace often reveals a lot of information. Consider if a check operation needs to be exposed
     * to everyone or just those who have higher access to job/hudson/etc.
     */
    public static FormValidation error(Throwable e, String message) {
        return _error(Kind.ERROR, e, message);
    }

    public static FormValidation warning(Throwable e, String message) {
        return _error(Kind.WARNING, e, message);
    }

    private static FormValidation _error(Kind kind, Throwable e, String message) {
        if (e==null)    return _errorWithMarkup(Util.escape(message),kind);

        return _errorWithMarkup(Util.escape(message)+
            " <a href='#' class='showDetails'>"
            + Messages.FormValidation_Error_Details()
            + "</a><pre style='display:none'>"
            + Functions.printThrowable(e) +
            "</pre>",kind
        );
    }

    public static FormValidation error(Throwable e, String format, Object... args) {
        return error(e,String.format(format,args));
    }

    public static FormValidation warning(Throwable e, String format, Object... args) {
        return warning(e,String.format(format,args));
    }



    /**
     * Sends out an HTML fragment that indicates an error.
     *
     * <p>
     * This method must be used with care to avoid cross-site scripting
     * attack.
     *
     * @param message
     *      Human readable message to be sent. <tt>error(null)</tt>
     *      can be used as <tt>ok()</tt>.
     */
    public static FormValidation errorWithMarkup(String message) {
        return _errorWithMarkup(message,Kind.ERROR);
    }

    public static FormValidation warningWithMarkup(String message) {
        return _errorWithMarkup(message,Kind.WARNING);
    }

    public static FormValidation okWithMarkup(String message) {
        return _errorWithMarkup(message,Kind.OK);
    }

    private static FormValidation _errorWithMarkup(final String message, final Kind kind) {
        if(message==null)
            return ok();
        return new FormValidation(kind, message) {
            public String renderHtml() {
                // 1x16 spacer needed for IE since it doesn't support min-height
                return "<div class="+ kind.name().toLowerCase(Locale.ENGLISH) +"><img src='"+
                        Stapler.getCurrentRequest().getContextPath()+ Hudson.RESOURCE_PATH+"/images/none.gif' height=16 width=1>"+
                        message+"</div>";
            }
        };
    }

    /**
     * Sends out an arbitrary HTML fragment as the output.
     */
    public static FormValidation respond(Kind kind, final String html) {
        return new FormValidation(kind) {
            public String renderHtml() {
                return html;
            }
        };
    }

    /**
     * Performs an application-specific validation on the given file.
     *
     * <p>
     * This is used as a piece in a bigger validation effort.
     */
    public static abstract class FileValidator {
        public abstract FormValidation validate(File f);

        /**
         * Singleton instance that does no check.
         */
        public static final FileValidator NOOP = new FileValidator() {
            public FormValidation validate(File f) {
                return ok();
            }
        };
    }

    /**
     * Makes sure that the given string points to an executable file.
     */
    public static FormValidation validateExecutable(String exe) {
        return validateExecutable(exe, FileValidator.NOOP);
    }

    /**
     * Makes sure that the given string points to an executable file.
     *
     * @param exeValidator
     *      If the validation process discovers a valid executable program on the given path,
     *      the specified {@link FileValidator} can perform additional checks (such as making sure
     *      that it has the right version, etc.)
     */
    public static FormValidation validateExecutable(String exe, FileValidator exeValidator) {
        // insufficient permission to perform validation?
        if(!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) return ok();

        exe = fixEmpty(exe);
        if(exe==null)
            return ok();

        if(exe.indexOf(File.separatorChar)>=0) {
            // this is full path
            File f = new File(exe);
            if(f.exists())  return exeValidator.validate(f);

            File fexe = new File(exe+".exe");
            if(fexe.exists())   return exeValidator.validate(fexe);

            return error("There's no such file: "+exe);
        }

        // look in PATH
        String path = EnvVars.masterEnvVars.get("PATH");
        String tokenizedPath = "";
        String delimiter = null;
        if(path!=null) {
            for (String _dir : Util.tokenize(path.replace("\\", "\\\\"),File.pathSeparator)) {
                if (delimiter == null) {
                  delimiter = ", ";
                }
                else {
                  tokenizedPath += delimiter;
                }

                tokenizedPath += _dir.replace('\\', '/');

                File dir = new File(_dir);

                File f = new File(dir,exe);
                if(f.exists())  return exeValidator.validate(f);

                File fexe = new File(dir,exe+".exe");
                if(fexe.exists())   return exeValidator.validate(fexe);
            }

            tokenizedPath += ".";
        } else {
            tokenizedPath = "unavailable.";
        }

        // didn't find it
        return error("There's no such executable "+exe+" in PATH: "+tokenizedPath);
    }

    /**
     * Makes sure that the given string is a non-negative integer.
     */
    public static FormValidation validateNonNegativeInteger(String value) {
        try {
            if(Integer.parseInt(value)<0)
                return error(hudson.model.Messages.Hudson_NotANonNegativeNumber());
            return ok();
        } catch (NumberFormatException e) {
            return error(hudson.model.Messages.Hudson_NotANumber());
        }
    }

    /**
     * Makes sure that the given string is a positive integer.
     */
    public static FormValidation validatePositiveInteger(String value) {
        try {
            if(Integer.parseInt(value)<=0)
                return error(hudson.model.Messages.Hudson_NotAPositiveNumber());
            return ok();
        } catch (NumberFormatException e) {
            return error(hudson.model.Messages.Hudson_NotANumber());
        }
    }

    /**
     * Makes sure that the given string is not null or empty.
     */
    public static FormValidation validateRequired(String value) {
        if (Util.fixEmptyAndTrim(value) == null)
            return error(Messages.FormValidation_ValidateRequired());
        return ok();
    }

    /**
     * Makes sure that the given string is a base64 encoded text.
     *
     * @param allowWhitespace
     *      if you allow whitespace (CR,LF,etc) in base64 encoding
     * @param allowEmpty
     *      Is empty string allowed?
     * @param errorMessage
     *      Error message.
     * @since 1.305
     */
    public static FormValidation validateBase64(String value, boolean allowWhitespace, boolean allowEmpty, String errorMessage) {
        try {
            String v = value;
            if(!allowWhitespace) {
                if(v.indexOf(' ')>=0 || v.indexOf('\n')>=0)
                    return error(errorMessage);
            }
            v=v.trim();
            if(!allowEmpty && v.length()==0)
                return error(errorMessage);

            com.trilead.ssh2.crypto.Base64.decode(v.toCharArray());
            return ok();
        } catch (IOException e) {
            return error(errorMessage);
        }
    }

    /**
     * Convenient base class for checking the validity of URLs.
     *
     * <p>
     * This allows the check method to call various utility methods in a concise syntax.
     */
    public static abstract class URLCheck {
        /**
         * Opens the given URL and reads text content from it.
         * This method honors Content-type header.
         */
        protected BufferedReader open(URL url) throws IOException {
            // use HTTP content type to find out the charset.
            URLConnection con = ProxyConfiguration.open(url);
            if (con == null) { // XXX is this even permitted by URL.openConnection?
                throw new IOException(url.toExternalForm());
            }
            return new BufferedReader(
                new InputStreamReader(con.getInputStream(),getCharset(con)));
        }

        /**
         * Finds the string literal from the given reader.
         * @return
         *      true if found, false otherwise.
         */
        protected boolean findText(BufferedReader in, String literal) throws IOException {
            String line;
            while((line=in.readLine())!=null)
                if(line.indexOf(literal)!=-1)
                    return true;
            return false;
        }

        /**
         * Calls the {@link FormValidation#error(String)} method with a reasonable error message.
         * Use this method when the {@link #open(URL)} or {@link #findText(BufferedReader, String)} fails.
         *
         * @param url
         *      Pass in the URL that was connected. Used for error diagnosis.
         */
        protected FormValidation handleIOException(String url, IOException e) throws IOException, ServletException {
            // any invalid URL comes here
            if(e.getMessage().equals(url))
                // Sun JRE (and probably others too) often return just the URL in the error.
                return error("Unable to connect "+url);
            else
                return error(e.getMessage());
        }

        /**
         * Figures out the charset from the content-type header.
         */
        private String getCharset(URLConnection con) {
            for( String t : con.getContentType().split(";") ) {
                t = t.trim().toLowerCase(Locale.ENGLISH);
                if(t.startsWith("charset="))
                    return t.substring(8);
            }
            // couldn't find it. HTML spec says default is US-ASCII,
            // but UTF-8 is a better choice since
            // (1) it's compatible with US-ASCII
            // (2) a well-written web applications tend to use UTF-8
            return "UTF-8";
        }

        /**
         * Implement the actual form validation logic, by using other convenience methosd defined in this class.
         * If you are not using any of those, you don't need to extend from this class.
         */
        protected abstract FormValidation check() throws IOException, ServletException;
    }


    //TODO: review and check whether we can do it private
    public final Kind kind;

    public Kind getKind() {
        return kind;
    }

    /**
     * Instances should be created via one of the factory methods above.
     * @param kind
     */
    private FormValidation(Kind kind) {
        this.kind = kind;
    }

    private FormValidation(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        respond(rsp, renderHtml());
    }

    public abstract String renderHtml();

    /**
     * Sends out an arbitrary HTML fragment as the output.
     */
    protected void respond(StaplerResponse rsp, String html) throws IOException, ServletException {
        rsp.setContentType("text/html;charset=UTF-8");
        rsp.getWriter().print(html);
    }
}
