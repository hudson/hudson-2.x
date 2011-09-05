/*******************************************************************************
 *
 * Copyright (c) 2004-2011, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * Nikita Levyankov
 *
 *******************************************************************************/
package hudson.util;

import hudson.FilePath;
import hudson.model.ExternalJob;
import hudson.model.Job;
import hudson.model.JobParameterValue;
import hudson.slaves.WorkspaceList;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Test to verify JSON processing.
 */
public class JSONTest extends TestCase {

    public void testJSONLeaseFields() {
        WorkspaceList.Lease dummyLease = WorkspaceList.Lease.createDummyLease(new FilePath(new File(".")));
        JSONObject json = JSONObject.fromObject(dummyLease);
        assertEquals(json.size(), 1);
    }

    public void testJSONListBoxModelOption() {
        ListBoxModel.Option option = new ListBoxModel.Option("somename", "somevalue", true);
        JSONObject json = JSONObject.fromObject(option);
        assertNotNull(json.get("name"));
    }

    public void testCanonicalWriter() throws IOException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(true);
        jsonArray.add(1);
        jsonArray.add(5.3);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", "1");
        jsonObject.put("key2", "2");
        jsonObject.put("key3", "3");
        jsonObject.put("string", "123\r\n\b\t\f\\\\u65E5\\u672C\\u8A9E");
        jsonArray.add(jsonObject);
        Writer writer = new StringWriter();
        JSONCanonicalUtils.write(jsonArray, writer);
        assertEquals(writer.toString(),
            "[true,1,5.3,{\"key1\":\"1\",\"key2\":\"2\",\"key3\":\"3\",\"string\":\"123\\u000d\\u000a\\u0008\\u0009\\u000c\\\\\\\\u65E5\\\\u672C\\\\u8A9E\"}]");
    }
}
