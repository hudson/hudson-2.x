/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 * Daniel Dyer, Tom Huybrechts, Anton Kozak
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

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import hudson.XmlFile;
import hudson.Util;
import hudson.Functions;
import hudson.BulkChange;
import hudson.cli.declarative.CLIMethod;
import hudson.cli.declarative.CLIResolver;
import hudson.model.listeners.ItemListener;
import hudson.model.listeners.SaveableListener;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.ACL;
import hudson.util.AtomicFileWriter;
import hudson.util.IOException2;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.HttpDeletable;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;

import javax.servlet.ServletException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * Partial default implementation of {@link Item}.
 *
 * @author Kohsuke Kawaguchi
 */
// Item doesn't necessarily have to be Actionable, but
// Java doesn't let multiple inheritance.
@ExportedBean
public abstract class AbstractItem extends Actionable implements Item, HttpDeletable, AccessControlled, DescriptorByNameOwner {
    /**
     * Project name.
     */
    protected /*final*/ transient String name;

    /**
     * Project description. Can be HTML.
     */
    protected volatile String description;

    private transient ItemGroup parent;

    protected AbstractItem(ItemGroup parent, String name) {
        this.parent = parent;
        doSetName(name);
    }

    public void onCreatedFromScratch() {
        // noop
    }

    @Exported(visibility=999)
    public String getName() {
        return name;
    }

    /**
     * Get the term used in the UI to represent this kind of
     * {@link Item}. Must start with a capital letter.
     */
    public String getPronoun() {
        return Messages.AbstractItem_Pronoun();
    }

    @Exported
    public String getDisplayName() {
        return getName();
    }

    public File getRootDir() {
        return (parent != null ? parent.getRootDirFor(this) : Hudson.getInstance().getRootDir());
    }

    /**
     * This bridge method is to maintain binary compatibility with {@link TopLevelItem#getParent()}.
     */
    @WithBridgeMethods(value=Hudson.class,castRequired=true)
    public ItemGroup getParent() {
        assert parent!=null;
        return parent;
    }

    /**
     * Gets the project description HTML.
     */
    @Exported
    public String getDescription() {
        return description;
    }

    /**
     * Sets the project description HTML.
     */
    public void setDescription(String description) throws IOException {
        this.description = description;
        save();
    }

    /**
     * Just update {@link #name} without performing the rename operation,
     * which would involve copying files and etc.
     */
    protected void doSetName(String name) {
        this.name = name;
    }

    /**
     * Ad additional action which should be performed before the item will be renamed.
     * It's possible to add some logic in the subclasses.
     *
     * @param oldName old item name.
     * @param newName new item name.
     * @throws java.io.IOException if item couldn't be saved.
     */
    protected void performBeforeItemRenaming(String oldName, String newName) throws IOException {
    }

    /**
     * Renames this item.
     * Not all the Items need to support this operation, but if you decide to do so,
     * you can use this method.
     */
    protected void renameTo(String newName) throws IOException {
        // always synchronize from bigger objects first
        final ItemGroup parent = getParent();
        synchronized (parent) {
            synchronized (this) {
                // sanity check
                if (newName == null)
                    throw new IllegalArgumentException("New name is not given");

                // noop?
                if (this.name.equals(newName))
                    return;

                Item existing = parent.getItem(newName);
                if (existing != null && existing!=this)
                    // the look up is case insensitive, so we need "existing!=this"
                    // to allow people to rename "Foo" to "foo", for example.
                    // see http://www.nabble.com/error-on-renaming-project-tt18061629.html
                    throw new IllegalArgumentException("Job " + newName
                            + " already exists");
                String oldName = this.name;

                performBeforeItemRenaming(oldName,newName);

                File oldRoot = this.getRootDir();

                doSetName(newName);
                File newRoot = this.getRootDir();

                boolean success = false;

                try {// rename data files
                    boolean interrupted = false;
                    boolean renamed = false;

                    // try to rename the job directory.
                    // this may fail on Windows due to some other processes
                    // accessing a file.
                    // so retry few times before we fall back to copy.
                    for (int retry = 0; retry < 5; retry++) {
                        if (oldRoot.renameTo(newRoot)) {
                            renamed = true;
                            break; // succeeded
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            // process the interruption later
                            interrupted = true;
                        }
                    }

                    if (interrupted)
                        Thread.currentThread().interrupt();

                    if (!renamed) {
                        // failed to rename. it must be that some lengthy
                        // process is going on
                        // to prevent a rename operation. So do a copy. Ideally
                        // we'd like to
                        // later delete the old copy, but we can't reliably do
                        // so, as before the VM
                        // shuts down there might be a new job created under the
                        // old name.
                        Copy cp = new Copy();
                        cp.setProject(new org.apache.tools.ant.Project());
                        cp.setTodir(newRoot);
                        FileSet src = new FileSet();
                        src.setDir(getRootDir());
                        cp.addFileset(src);
                        cp.setOverwrite(true);
                        cp.setPreserveLastModified(true);
                        cp.setFailOnError(false); // keep going even if
                                                    // there's an error
                        cp.execute();

                        // try to delete as much as possible
                        try {
                            Util.deleteRecursive(oldRoot);
                        } catch (IOException e) {
                            // but ignore the error, since we expect that
                            e.printStackTrace();
                        }
                    }

                    success = true;
                } finally {
                    // if failed, back out the rename.
                    if (!success)
                        doSetName(oldName);
                }

                callOnRenamed(newName, parent, oldName);

                for (ItemListener l : ItemListener.all())
                    l.onRenamed(this, oldName, newName);
            }
        }
    }

    /**
     * A pointless function to work around what appears to be a HotSpot problem. See HUDSON-5756 and bug 6933067
     * on BugParade for more details.
     */
    private void callOnRenamed(String newName, ItemGroup parent, String oldName) throws IOException {
        try {
            parent.onRenamed(this, oldName, newName);
        } catch (AbstractMethodError _) {
            // ignore
        }
    }

    /**
     * Gets all the jobs that this {@link Item} contains as descendants.
     */
    public abstract Collection<? extends Job> getAllJobs();

    public final String getFullName() {
        String n = getParent().getFullName();
        if(n.length()==0)   return getName();
        else                return n+'/'+getName();
    }

    public final String getFullDisplayName() {
        String n = getParent().getFullDisplayName();
        if(n.length()==0)   return getDisplayName();
        else                return n+" \u00BB "+getDisplayName();
    }

    /**
     * Called right after when a {@link Item} is loaded from disk.
     * This is an opporunity to do a post load processing.
     */
    public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
        this.parent = parent;
        doSetName(name);
    }

    /**
     * When a {@link Item} is copied from existing one,
     * the files are first copied on the file system,
     * then it will be loaded, then this method will be invoked
     * to perform any implementation-specific work.
     */
    public void onCopiedFrom(Item src) {
    }

    public final String getUrl() {
        // try to stick to the current view if possible
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req != null) {
            String seed = Functions.getNearestAncestorUrl(req,this);
            if(seed!=null) {
                // trim off the context path portion and leading '/', but add trailing '/'
                return seed.substring(req.getContextPath().length()+1)+'/';
            }
        }

        // otherwise compute the path normally
        return getParent().getUrl()+getShortUrl();
    }

    public String getShortUrl() {
        return getParent().getUrlChildPrefix()+'/'+Util.rawEncode(getName())+'/';
    }

    public String getSearchUrl() {
        return getShortUrl();
    }

    @Exported(visibility=999,name="url")
    public final String getAbsoluteUrl() {
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request==null)
            throw new IllegalStateException("Not processing a HTTP request");
        return Util.encode(Hudson.getInstance().getRootUrl()+getUrl());
    }

    /**
     * Remote API access.
     */
    public final Api getApi() {
        return new Api(this);
    }

    /**
     * Returns the {@link ACL} for this object.
     */
    public ACL getACL() {
        return Hudson.getInstance().getAuthorizationStrategy().getACL(this);
    }

    /**
     * Short for {@code getACL().checkPermission(p)}
     */
    public void checkPermission(Permission p) {
        getACL().checkPermission(p);
    }

    /**
     * Short for {@code getACL().hasPermission(p)}
     */
    public boolean hasPermission(Permission p) {
        return getACL().hasPermission(p);
    }

    /**
     * Save the settings to a file.
     */
    public synchronized void save() throws IOException {
        if(BulkChange.contains(this))   return;
        getConfigFile().write(this);
        SaveableListener.fireOnChange(this, getConfigFile());
    }

    public final XmlFile getConfigFile() {
        return Items.getConfigFile(this);
    }

    public Descriptor getDescriptorByName(String className) {
        return Hudson.getInstance().getDescriptorByName(className);
    }

    /**
     * Accepts the new description.
     */
    public synchronized void doSubmitDescription( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        checkPermission(CONFIGURE);

        setDescription(req.getParameter("description"));
        rsp.sendRedirect(".");  // go to the top page
    }

    /**
     * Deletes this item.
     */
    @CLIMethod(name="delete-job")
    public void doDoDelete( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, InterruptedException {
        requirePOST();
        delete();
        if (rsp != null) // null for CLI
            rsp.sendRedirect2(req.getContextPath()+"/"+getParent().getUrl());
    }

    public void delete( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        try {
            doDoDelete(req,rsp);
        } catch (InterruptedException e) {
            // TODO: allow this in Stapler
            throw new ServletException(e);
        }
    }

    /**
     * Deletes this item.
     */
    public synchronized void delete() throws IOException, InterruptedException {
        checkPermission(DELETE);
        performDelete();

        try {
            invokeOnDeleted();
        } catch (AbstractMethodError e) {
            // ignore
        }

        Hudson.getInstance().rebuildDependencyGraph();
    }

    /**
     * A pointless function to work around what appears to be a HotSpot problem. See HUDSON-5756 and bug 6933067
     * on BugParade for more details.
     */
    private void invokeOnDeleted() throws IOException {
        getParent().onDeleted(this);
    }

    /**
     * Does the real job of deleting the item.
     */
    protected void performDelete() throws IOException, InterruptedException {
        getConfigFile().delete();
        Util.deleteRecursive(getRootDir());
    }

    /**
     * Accepts <tt>config.xml</tt> submission, as well as serve it.
     */
    @WebMethod(name = "config.xml")
    public void doConfigDotXml(StaplerRequest req, StaplerResponse rsp)
            throws IOException {
        if (req.getMethod().equals("GET")) {
            // read
            checkPermission(EXTENDED_READ);
            rsp.setContentType("application/xml");
            getConfigFile().writeRawTo(rsp.getOutputStream());
            return;
        }
        if (req.getMethod().equals("POST")) {
            // submission
            checkPermission(CONFIGURE);
            XmlFile configXmlFile = getConfigFile();
            AtomicFileWriter out = new AtomicFileWriter(configXmlFile.getFile());
            try {
                try {
                    // this allows us to use UTF-8 for storing data,
                    // plus it checks any well-formedness issue in the submitted
                    // data
                    Transformer t = TransformerFactory.newInstance()
                            .newTransformer();
                    t.transform(new StreamSource(req.getReader()),
                            new StreamResult(out));
                    out.close();
                } catch (TransformerException e) {
                    throw new IOException2("Failed to persist configuration.xml", e);
                }

                // try to reflect the changes by reloading
                new XmlFile(Items.XSTREAM, out.getTemporaryFile()).unmarshal(this);
                onLoad(getParent(), getRootDir().getName());

                // if everything went well, commit this new version
                out.commit();
            } finally {
                out.abort(); // don't leave anything behind
            }
            return;
        }

        // huh?
        rsp.sendError(SC_BAD_REQUEST);
    }

    public String toString() {
        return getClass().getSimpleName()+'['+getFullName()+']';
    }

    /**
     * Used for CLI binding.
     */
    @CLIResolver
    public static AbstractItem resolveForCLI(
            @Argument(required=true,metaVar="NAME",usage="Job name") String name) throws CmdLineException {
        AbstractItem item = Hudson.getInstance().getItemByFullName(name, AbstractItem.class);
        if (item==null)
            throw new CmdLineException(null,Messages.AbstractItem_NoSuchJobExists(name,AbstractProject.findNearest(name).getFullName()));
        return item;
    }
}
