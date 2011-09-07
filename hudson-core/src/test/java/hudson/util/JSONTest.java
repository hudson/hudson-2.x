/*
* The MIT License
*
* Copyright (c) 2011, Oracle Corporation
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
 * Test to verify GSON processing.
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
