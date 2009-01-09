package ${groupId};

import hudson.Launcher;
import hudson.util.FormFieldValidator;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method
 * will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class HelloWorldBuilder extends Builder {

    private final String name;

    @DataBoundConstructor
    public HelloWorldBuilder(String name) {
        this.name = name;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    public boolean perform(Build build, Launcher launcher, BuildListener listener) {
        // this is where you 'build' the project
        // since this is a dummy, we just say 'hello world' and call that a build

        // this also shows how you can consult the global configuration of the builder
        if(DESCRIPTOR.useFrench())
            listener.getLogger().println("Bonjour, "+name+"!");
        else
            listener.getLogger().println("Hello, "+name+"!");
        return true;
    }

    public Descriptor<Builder> getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return DESCRIPTOR;
    }

    /**
     * Descriptor should be singleton.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    public static final class DescriptorImpl extends Descriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private boolean useFrench;

        DescriptorImpl() {
            super(HelloWorldBuilder.class);
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This receives the current value of the field.
         */
        public void doCheckName(StaplerRequest req, StaplerResponse rsp, @QueryParameter final String value) throws IOException, ServletException {
            new FormFieldValidator(req,rsp,null) {
                /**
                 * The real check goes here. In the end, depending on which
                 * method you call, the browser shows text differently.
                 */
                protected void check() throws IOException, ServletException {
                    if(value.length()==0)
                        error("Please set a name");
                    else
                    if(value.length()<4)
                        warning("Isn't the name too short?");
                    else
                        ok();

                }
            }.process();
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Say hello world";
        }

        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            // to persist global configuration information,
            // set that to properties and call save().
            useFrench = o.getBoolean("useFrench");
            save();
            return super.configure(req);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         */
        public boolean useFrench() {
            return useFrench;
        }
    }
}

