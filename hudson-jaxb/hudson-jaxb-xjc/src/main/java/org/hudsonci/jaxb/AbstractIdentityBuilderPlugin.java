/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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


package org.hudsonci.jaxb;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.w3c.dom.Attr;

import javax.xml.namespace.QName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for plugins creating identity methods (equals and hashcode).
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public abstract class AbstractIdentityBuilderPlugin
    extends AbstractParameterizablePlugin
{
    public static final String NAMESPACE_URI = "http://hudson-ci.org/jaxb/hudson/id-builder";
    private static final String TAG_EXCLUDE = "exclude";
    private static final String ATTRIBUTE_FIELDS = "fields";
    private static final String FIELD_DELIMITER = ",";
    
    private List<String> excludedFields;

    @Override
    public Collection<QName> getCustomizationElementNames() {
        return Arrays.asList(new QName(NAMESPACE_URI, TAG_EXCLUDE));
    }

    @Override
    protected boolean run(final Outline outline, final Options options) throws Exception {
        assert outline != null;
        assert options != null;
    
        for (ClassOutline type : outline.getClasses()) {
            // Set-up data used for processing.
            excludedFields = findFieldsForTag(type, TAG_EXCLUDE);
            processClassOutline(type);
        }
    
        return true;
    }

    protected boolean isFieldApplicable(FieldOutline field) {
        return !excludedFields.contains(field.getPropertyInfo().getName(false));
    }

    abstract void processClassOutline(ClassOutline type);

    private List<String> findFieldsForTag(final ClassOutline outline, final String tag) {
        List<String> fields = Collections.emptyList();
        
        CPluginCustomization customization = outline.target.getCustomizations().find(NAMESPACE_URI, tag);
        if (customization != null) {
            Attr attributeNode = customization.element.getAttributeNode(ATTRIBUTE_FIELDS);
            if (attributeNode != null) {
                customization.markAsAcknowledged();
    
                String fieldsValue = attributeNode.getValue();
                fields = Arrays.asList(fieldsValue.replace(" ", "").split(FIELD_DELIMITER));
            }
        }
        return fields;
    }
}
