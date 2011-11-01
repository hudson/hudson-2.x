/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Anton Kozak
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
package hudson.matrix;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Hudson;
import hudson.util.QuotedStringTokenizer;
import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

/**
 * Configuration axis.
 *
 * <p>
 * This class represents a single dimension of the configuration matrix.
 * For example, the JAX-WS RI test configuration might include
 * one axis "container={glassfish,tomcat,jetty}" and another axis
 * "stax={sjsxp,woodstox}", and so on.
 *
 * @author Kohsuke Kawaguchi
 */
public class Axis extends AbstractDescribableImpl<Axis> implements Comparable<Axis>, Iterable<String>, ExtensionPoint {
    /**
     * Name of this axis.
     * Used as a variable name.
     *
     * @deprecated as of 1.373
     *      Use {@link #getName()}
     */
    public final String name;

    /**
     * Possible values for this axis.
     *
     * @deprecated as of 1.373
     *      Use {@link #getValues()}
     */
    //TODO: review and check whether we can do it private
    public final List<String> values;

    public Axis(String name, List<String> values) {
        this.name = name;
        this.values = new ArrayList<String>(values);
        if(values.isEmpty())
            throw new IllegalArgumentException(); // bug in the code
    }

    public Axis(String name, String... values) {
        this(name,Arrays.asList(values));        
    }

    /**
     * Used to build {@link Axis} from form.
     *
     * Axis with empty values need to be removed later.
     */
    @DataBoundConstructor
    public Axis(String name, String valueString) {
        this.name = name;
        this.values = new ArrayList<String>(Arrays.asList(Util.tokenize(valueString)));
    }

    /**
     * Returns true if this axis is a system-reserved axis
     * that <strike>has</strike> used to have af special treatment.
     *
     * @deprecated as of 1.373
     *      System vs user difference are generalized into extension point.
     */
    public boolean isSystem() {
        return false;
    }

    public Iterator<String> iterator() {
        return getValues().iterator();
    }

    public int size() {
        return getValues().size();
    }

    public String value(int index) {
        return getValues().get(index);
    }

    /**
     * The inverse of {@link #value(int)}.
     */
    public int indexOf(String value) {
        return values.indexOf(value);
    }

    /**
     * Axis is fully ordered so that we can convert between a list of axis
     * and a string unambiguously.
     */
    public int compareTo(Axis that) {
        return this.name.compareTo(that.name);
    }

    /**
     * Name of this axis.
     * Used as a variable name.
     */
    public String getName() {
        return name;
    }

    /**
     * Possible values for this axis.
     */
    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public AxisDescriptor getDescriptor() {
        return (AxisDescriptor)super.getDescriptor();
    }

    @Override
    public String toString() {
        return new StringBuilder().append(name).append("={").append(Util.join(values,",")).append('}').toString();
    }

    /**
     * Used for generating the config UI.
     * If the axis is big and occupies a lot of space, use newline for separator
     * to display multi-line text.
     */
    public String getValueString() {
        int len=0;
        for (String value : values)
            len += value.length();
        char delim = len>30 ? '\n' : ' ';
        // Build string connected with delimiter, quoting as needed
        StringBuilder buf = new StringBuilder(len+values.size()*3);
        for (String value : values)
            buf.append(delim).append(QuotedStringTokenizer.quote(value,""));
        return buf.substring(1);
    }

    /**
     * Parses the submitted form (where possible values are
     * presented as a list of checkboxes) and creates an axis
     */
    public static Axis parsePrefixed(StaplerRequest req, String name) {
        List<String> values = new ArrayList<String>();
        String prefix = name+'.';

        Enumeration e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            if(paramName.startsWith(prefix))
                values.add(paramName.substring(prefix.length()));
        }
        if(values.isEmpty())
            return null;
        return new Axis(name,values);
    }

    /**
     * Previously we used to persist {@link Axis}, but now those are divided into subtypes.
     * So upon deserialization, resolve to the proper type.
     */
    public Object readResolve() {
        if (getClass()!=Axis.class) return this;

        if (getName().equals("jdk"))
            return new JDKAxis(getValues());
        if (getName().equals("label"))
            return new LabelAxis(getName(),getValues());
        return new TextAxis(getName(),getValues());
    }

    /**
     * Returns all the registered {@link AxisDescriptor}s.
     */
    public static DescriptorExtensionList<Axis,AxisDescriptor> all() {
        return Hudson.getInstance().<Axis,AxisDescriptor>getDescriptorList(Axis.class);
    }

    /**
     * Converts the selected value (which is among {@link #values}) and adds that to the given map,
     * which serves as the build variables.
     */
    public void addBuildVariable(String value, Map<String,String> map) {
        map.put(name,value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Axis axis = (Axis) o;

        if (name != null ? !name.equals(axis.name) : axis.name != null) {
            return false;
        }

        if (values != null ? !CollectionUtils.isEqualCollection(values, axis.values) : axis.values != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }
}
