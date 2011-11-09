/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Daniel Dyer, Stephen Connolly
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
package hudson.scm;

import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.util.DescriptorList;
import hudson.Extension;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

/**
 * List of all installed {@link RepositoryBrowsers}.
 *
 * @author Kohsuke Kawaguchi
 */
public class RepositoryBrowsers {
    /**
     * List of all installed {@link RepositoryBrowsers}.
     *
     * @deprecated as of 1.286.
     *      Use {@link RepositoryBrowser#all()} for read access and {@link Extension} for registration.
     */
    public static final List<Descriptor<RepositoryBrowser<?>>> LIST = new DescriptorList<RepositoryBrowser<?>>((Class)RepositoryBrowser.class);

    /**
     * Only returns those {@link RepositoryBrowser} descriptors that extend from the given type.
     */
    public static List<Descriptor<RepositoryBrowser<?>>> filter(Class<? extends RepositoryBrowser> t) {
        List<Descriptor<RepositoryBrowser<?>>> r = new ArrayList<Descriptor<RepositoryBrowser<?>>>();
        for (Descriptor<RepositoryBrowser<?>> d : RepositoryBrowser.all())
            if(d.isSubTypeOf(t))
                r.add(d);
        return r;
    }

    /**
     * Creates an instance of {@link RepositoryBrowser} from a form submission.
     *
     * @deprecated since 2008-06-19.
     *      Use {@link #createInstance(Class, StaplerRequest, JSONObject, String)}.
     */
    public static <T extends RepositoryBrowser>
    T createInstance(Class<T> type, StaplerRequest req, String fieldName) throws FormException {
        List<Descriptor<RepositoryBrowser<?>>> list = filter(type);
        String value = req.getParameter(fieldName);
        if(value==null || value.equals("auto"))
            return null;

        return type.cast(list.get(Integer.parseInt(value)).newInstance(req,null/*TODO*/));
    }

    /**
     * Creates an instance of {@link RepositoryBrowser} from a form submission.
     *
     * @since 1.227
     */
    public static <T extends RepositoryBrowser>
    T createInstance(Class<T> type, StaplerRequest req, JSONObject parent, String fieldName) throws FormException {
        JSONObject o = (JSONObject)parent.get(fieldName);
        if(o==null) return null;

        return req.bindJSON(type,o);
    }
}
