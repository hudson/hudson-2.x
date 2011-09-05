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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONFunction;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONString;
import net.sf.json.util.JSONUtils;

/**
 * Json utils for canonical writing.
 */
public class JSONCanonicalUtils {
    /**
     * Write JSON object to writer in canonical form.
     *
     * @param json JSONObject
     * @param writer Writer
     * @throws IOException if any
     */
    public static void write(JSONObject json, Writer writer) throws IOException {

        if (json.isNullObject()) {
            writer.write(JSONNull.getInstance().toString());
            return;
        }

        boolean b = false;
        Iterator keys = json.keys();
        writer.write('{');

        while (keys.hasNext()) {
            if (b) {
                writer.write(',');
            }
            Object k = keys.next();
            writer.write(JSONUtils.quote(k.toString()));
            writer.write(':');
            Object v = json.get(k);
            if (v instanceof JSON) {
                write((JSON) v, writer);
            } else {
                writer.write(toCanonical(v));
            }
            b = true;
        }
        writer.write('}');
    }

    /**
     * Write JSON array to writer in canonical form.
     *
     * @param json JSONArray
     * @param writer Writer
     * @throws IOException if any.
     */
    public static void write(JSONArray json, Writer writer) throws IOException {
        boolean b = false;
        int len = json.size();
        writer.write('[');
        for (int i = 0; i < len; i += 1) {
            if (b) {
                writer.write(',');
            }
            Object v = json.get(i);
            if (v instanceof JSON) {
                write((JSON) v, writer);
            } else {
                writer.write(toCanonical(v));
            }
            b = true;
        }
        writer.write(']');
    }

    private static void write(JSON o, Writer writer) throws IOException {
        if (o instanceof JSONObject) {
            write((JSONObject) o, writer);
        } else if (o instanceof JSONArray) {
            write((JSONArray) o, writer);
        }
    }

    private static String toCanonical(Object value) throws IOException {
        if (value == null || JSONUtils.isNull(value)) {
            return "null";
        }
        if (value instanceof JSONFunction) {
            return value.toString();
        }
        if (value instanceof JSONString) {
            return ((JSONString) value).toJSONString();
        }
        if (value instanceof Number) {
            return JSONUtils.numberToString((Number) value).toLowerCase();
        }
        if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray) {
            return value.toString();
        }
        return quoteCanonical(value.toString());
    }

    private static String quoteCanonical(String s) {
        if (s == null || s.length() == 0) {
            return "\"\"";
        }

        int len = s.length();
        StringBuilder sb = new StringBuilder(len + 4);

        sb.append('"');
        for (int i = 0; i < len; i += 1) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u")
                            .append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
