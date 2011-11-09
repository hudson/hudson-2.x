/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
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
package hudson.model;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.util.IOUtils;
import hudson.util.QuotedStringTokenizer;
import hudson.util.TextFile;
import hudson.util.TimeUnit2;
import org.kohsuke.stapler.Stapler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Service for plugins to periodically retrieve update data files
 * (like the one in the update center) through browsers.
 *
 * <p>
 * Because the retrieval of the file goes through XmlHttpRequest,
 * we cannot reliably pass around binary.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class DownloadService extends PageDecorator {
    public DownloadService() {
        super(DownloadService.class);
    }

    /**
     * Builds up an HTML fragment that starts all the download jobs.
     */
    public String generateFragment() {
    	if (neverUpdate) return "";
    	
        StringBuilder buf = new StringBuilder();
        if(Hudson.getInstance().hasPermission(Hudson.READ)) {
            long now = System.currentTimeMillis();
            for (Downloadable d : Downloadable.all()) {
                if(d.getDue()<now && d.lastAttempt+10*1000<now) {
                    buf.append("<script>")
                       .append("Behaviour.addLoadEvent(function() {")
                       .append("  downloadService.download(")
                       .append(QuotedStringTokenizer.quote(d.getId()))
                       .append(',')
                       .append(QuotedStringTokenizer.quote(d.getUrl()))
                       .append(',')
                       .append("{version:")
                       .append(QuotedStringTokenizer.quote(Hudson.VERSION + "-" + Util.getDigestOf(Hudson.getInstance().getSecretKey())))
                       .append('}')
                       .append(',')
                       .append(QuotedStringTokenizer.quote(Stapler.getCurrentRequest().getContextPath()+'/'+getUrl()+"/byId/"+d.getId()+"/postBack"))
                       .append(',')
                       .append("null);")
                       .append("});")
                       .append("</script>");
                    d.lastAttempt = now;
                }
            }
        }
        return buf.toString();
    }

    /**
     * Gets {@link Downloadable} by its ID.
     * Used to bind them to URL.
     */
    public Downloadable getById(String id) {
        for (Downloadable d : Downloadable.all())
            if(d.getId().equals(id))
                return d;
        return null;
    }

    /**
     * Represents a periodically updated JSON data file obtained from a remote URL.
     *
     * <p>
     * This mechanism is one of the basis of the update center, which involves fetching
     * up-to-date data file.
     *
     * @since 1.305
     */
    public static class Downloadable implements ExtensionPoint {
        private final String id;
        private final String url;
        private final long interval;
        private volatile long due=0;
        private volatile long lastAttempt=Long.MIN_VALUE;

        /**
         *
         * @param url
         *      URL relative to {@link UpdateCenter#getUrl()}.
         *      So if this string is "foo.json", the ultimate URL will be
         *      something like "https://hudson.java.net/foo.json"
         *
         *      For security and privacy reasons, we don't allow the retrieval
         *      from random locations.
         */
        public Downloadable(String id, String url, long interval) {
            this.id = id;
            this.url = url;
            this.interval = interval;
        }

        /**
         * Uses the class name as an ID.
         */
        public Downloadable(Class id) {
            this(id.getName().replace('$','.'));
        }

        public Downloadable(String id) {
            this(id,id+".json");
        }

        public Downloadable(String id, String url) {
            this(id,url,TimeUnit2.DAYS.toMillis(1));
        }

        public String getId() {
            return id;
        }

        /**
         * URL to download.
         */
        public String getUrl() {
            return Hudson.getInstance().getUpdateCenter().getDefaultBaseUrl()+"updates/"+url;
        }

        /**
         * How often do we retrieve the new image?
         *
         * @return
         *      number of milliseconds between retrieval.
         */
        public long getInterval() {
            return interval;
        }

        /**
         * This is where the retrieved file will be stored.
         */
        public TextFile getDataFile() {
            return new TextFile(new File(Hudson.getInstance().getRootDir(),"updates/"+id));
        }

        /**
         * When shall we retrieve this file next time?
         */
        public long getDue() {
            if(due==0)
                // if the file doesn't exist, this code should result
                // in a very small (but >0) due value, which should trigger
                // the retrieval immediately.
                due = getDataFile().file.lastModified()+interval;
            return due;
        }

        /**
         * Loads the current file into JSON and returns it, or null
         * if no data exists.
         */
        public JSONObject getData() throws IOException {
            TextFile df = getDataFile();
            if(df.exists())
                return JSONObject.fromObject(df.read());
            return null;
        }

        /**
         * This is where the browser sends us the data. 
         */
        public void doPostBack(StaplerRequest req, StaplerResponse rsp) throws IOException {
            long dataTimestamp = System.currentTimeMillis();
            TextFile df = getDataFile();
            df.write(IOUtils.toString(req.getInputStream(),"UTF-8"));
            df.file.setLastModified(dataTimestamp);
            due = dataTimestamp+getInterval();
            LOGGER.info("Obtained the updated data file for "+id);
            rsp.setContentType("text/plain");  // So browser won't try to parse response
        }

        /**
         * Returns all the registered {@link Downloadable}s.
         */
        public static ExtensionList<Downloadable> all() {
            return Hudson.getInstance().getExtensionList(Downloadable.class);
        }

        /**
         * Returns the {@link Downloadable} that has the given ID.
         */
        public static Downloadable get(String id) {
            for (Downloadable d : all()) {
                if(d.id.equals(id))
                    return d;
            }
            return null;
        }

        private static final Logger LOGGER = Logger.getLogger(Downloadable.class.getName());
    }

    public static boolean neverUpdate = Boolean.getBoolean(DownloadService.class.getName()+".never");
}

