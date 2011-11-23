/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Inc., Kohsuke Kawaguchi, Nikita Levyankov
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

import com.thoughtworks.xstream.XStream;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.XmlFile;
import hudson.matrix.Axis;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.util.DescriptorList;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import org.hudsonci.model.project.property.AxisListProjectProperty;
import org.hudsonci.model.project.property.BaseProjectProperty;
import org.hudsonci.model.project.property.BooleanProjectProperty;
import org.hudsonci.model.project.property.CopyOnWriteListProjectProperty;
import org.hudsonci.model.project.property.DescribableListProjectProperty;
import org.hudsonci.model.project.property.ExternalProjectProperty;
import org.hudsonci.model.project.property.IntegerProjectProperty;
import org.hudsonci.model.project.property.LogRotatorProjectProperty;
import org.hudsonci.model.project.property.ResultProjectProperty;
import org.hudsonci.model.project.property.SCMProjectProperty;
import org.hudsonci.model.project.property.StringProjectProperty;
import org.hudsonci.model.project.property.TriggerProjectProperty;

/**
 * Convenience methods related to {@link Item}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class Items {
    /**
     * List of all installed {@link TopLevelItem} types.
     *
     * @deprecated as of 1.286
     *      Use {@link #all()} for read access and {@link Extension} for registration.
     */
    public static final List<TopLevelItemDescriptor> LIST = (List)new DescriptorList<TopLevelItem>(TopLevelItem.class);

    /**
     * Returns all the registered {@link TopLevelItemDescriptor}s.
     */
    public static DescriptorExtensionList<TopLevelItem,TopLevelItemDescriptor> all() {
        return Hudson.getInstance().<TopLevelItem,TopLevelItemDescriptor>getDescriptorList(TopLevelItem.class);
    }

    public static TopLevelItemDescriptor getDescriptor(String fqcn) {
        return Descriptor.find(all(),fqcn);
    }

    /**
     * Converts a list of items into a comma-separated list of full names.
     */
    public static String toNameList(Collection<? extends Item> items) {
        StringBuilder buf = new StringBuilder();
        for (Item item : items) {
            if(buf.length()>0)
                buf.append(", ");
            buf.append(item.getFullName());
        }
        return buf.toString();
    }

    /**
     * Does the opposite of {@link #toNameList(Collection)}.
     */
    public static <T extends Item> List<T> fromNameList(String list, Class<T> type) {
        Hudson hudson = Hudson.getInstance();

        List<T> r = new ArrayList<T>();
        StringTokenizer tokens = new StringTokenizer(list,",");
        while(tokens.hasMoreTokens()) {
            String fullName = tokens.nextToken().trim();
            T item = hudson.getItemByFullName(fullName,type);
            if(item!=null)
                r.add(item);
        }
        return r;
    }

    /**
     * Loads a {@link Item} from a config file.
     *
     * @param dir
     *      The directory that contains the config file, not the config file itself.
     */
    public static Item load(ItemGroup parent, File dir) throws IOException {
        Item item = (Item)getConfigFile(dir).read();
        item.onLoad(parent,dir.getName());
        return item;
    }

    /**
     * The file we save our configuration.
     */
    public static XmlFile getConfigFile(File dir) {
        return new XmlFile(XSTREAM,new File(dir,"config.xml"));
    }

    /**
     * The file we save our configuration.
     */
    public static XmlFile getConfigFile(Item item) {
        return getConfigFile(item.getRootDir());
    }

    /**
     * Used to load/save job configuration.
     *
     * When you extend {@link Job} in a plugin, try to put the alias so
     * that it produces a reasonable XML.
     */
    public static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("project",FreeStyleProject.class);
        XSTREAM.alias("matrix-project",MatrixProject.class);
        XSTREAM.alias("axis", Axis.class);
        XSTREAM.alias("matrix-config",MatrixConfiguration.class);

        //aliases for project properties.
        //TODO: think about migrating to xstream's annotations.
        XSTREAM.alias("base-property", BaseProjectProperty.class);
        XSTREAM.alias("external-property", ExternalProjectProperty.class);
        XSTREAM.alias("trigger-property", TriggerProjectProperty.class);
        XSTREAM.alias("integer-property", IntegerProjectProperty.class);
        XSTREAM.alias("boolean-property", BooleanProjectProperty.class);
        XSTREAM.alias("string-property", StringProjectProperty.class);
        XSTREAM.alias("log-rotator-property", LogRotatorProjectProperty.class);
        XSTREAM.alias("result-property", ResultProjectProperty.class);
        XSTREAM.alias("scm-property", SCMProjectProperty.class);

        XSTREAM.alias("copy-write-list-property", CopyOnWriteListProjectProperty.class);
        XSTREAM.alias("axis-list-property", AxisListProjectProperty.class);
        XSTREAM.alias("describable-list-property", DescribableListProjectProperty.class);
        XSTREAM.aliasField("project-properties", Job.class, "jobProperties");
        XSTREAM.aliasField("cascading-job-properties", Job.class, "cascadingJobProperties");
        XSTREAM.alias("appointed-node-property", AppointedNode.class);
    }
}
