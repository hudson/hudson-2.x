/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi, Ullrich Hafner
 *     
 *
 *******************************************************************************/ 

package hudson.remoting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.ZipFile;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Locates where a given class is loaded from.
 *
 * @author Kohsuke Kawaguchi
 */
public class Which {
    /**
     * Locates the jar file that contains the given class.
     *
     * @throws IllegalArgumentException
     *      if failed to determine.
     */
    public static URL jarURL(Class clazz) throws IOException {
        ClassLoader cl = clazz.getClassLoader();
        if(cl==null)
            cl = ClassLoader.getSystemClassLoader();
        URL res = cl.getResource(clazz.getName().replace('.', '/') + ".class");
        if(res==null)
            throw new IllegalArgumentException("Unable to locate class file for "+clazz);
        return res;
    }

    /**
     * Locates the jar file that contains the given class.
     *
     * <p>
     * Note that jar files are not always loaded from {@link File},
     * so for diagnostics purposes {@link #jarURL(Class)} is preferrable.
     *
     * @throws IllegalArgumentException
     *      if failed to determine.
     */
    public static File jarFile(Class clazz) throws IOException {
        URL res = jarURL(clazz);
        String resURL = res.toExternalForm();
        String originalURL = resURL;
        if(resURL.startsWith("jar:file:") || resURL.startsWith("wsjar:file:"))
            return fromJarUrlToFile(resURL);

        if(resURL.startsWith("code-source:/")) {
            // OC4J apparently uses this. See http://www.nabble.com/Hudson-on-OC4J-tt16702113.html
            resURL = resURL.substring("code-source:/".length(), resURL.lastIndexOf('!')); // cut off jar: and the file name portion
            return new File(decode(new URL("file:/"+resURL).getPath()));
        }
        
        if(resURL.startsWith("zip:")){
            // weblogic uses this. See http://www.nabble.com/patch-to-get-Hudson-working-on-weblogic-td23997258.html
            // also see http://www.nabble.com/Re%3A-Hudson-on-Weblogic-10.3-td25038378.html#a25043415
            resURL = resURL.substring("zip:".length(), resURL.lastIndexOf('!')); // cut off zip: and the file name portion
            return new File(decode(new URL("file:"+resURL).getPath()));
        }

        if(resURL.startsWith("file:")) {
            // unpackaged classes
            int n = clazz.getName().split("\\.").length; // how many slashes do wo need to cut?
            for( ; n>0; n-- ) {
                int idx = Math.max(resURL.lastIndexOf('/'), resURL.lastIndexOf('\\'));
                if(idx<0)   throw new IllegalArgumentException(originalURL + " - " + resURL);
                resURL = resURL.substring(0,idx);
            }

            // won't work if res URL contains ' '
            // return new File(new URI(null,new URL(res).toExternalForm(),null));
            // won't work if res URL contains '%20'
            // return new File(new URL(res).toURI());

            return new File(decode(new URL(resURL).getPath()));
        }

        if(resURL.startsWith("vfszip:")) {
            // JBoss5
            InputStream is = res.openStream();
            try {
                Object delegate = is;
                try {
                    while (delegate.getClass().getEnclosingClass()!=ZipFile.class) {
                        Field f = delegate.getClass().getDeclaredField("delegate");
                        f.setAccessible(true);
                        delegate = f.get(delegate);
                    }
                } catch (NoSuchFieldException e) {
                    // extra step for JDK6u24; zip internals have changed
                    Field f = delegate.getClass().getDeclaredField("is");
                    f.setAccessible(true);
                    delegate = f.get(delegate);
                }
                Field f = delegate.getClass().getDeclaredField("this$0");
                f.setAccessible(true);
                ZipFile zipFile = (ZipFile)f.get(delegate);
                return new File(zipFile.getName());
            } catch (Throwable e) {
                // something must have changed in JBoss5. fall through
                LOGGER.log(Level.FINE, "Failed to resolve vfszip into a jar location",e);
            } finally {
                is.close();
            }
        }

        if(resURL.startsWith("vfs:") || resURL.startsWith("vfsfile:")) {
            // JBoss6
            try {
                String resource = '/' + clazz.getName().replace('.', '/');
                resURL = resURL.substring(0, resURL.lastIndexOf(resource));
                Object content = new URL(res, resURL).getContent();
                if (content instanceof File) {
                    return (File)content;
                }
                Method m = content.getClass().getMethod( "getPhysicalFile" );
                return (File)m.invoke(content);
            } catch ( Throwable e ) {
                // something must have changed in JBoss6. fall through
                LOGGER.log(Level.FINE, "Failed to resolve vfs/vfsfile into a jar location",e);
            }
        }

        if(resURL.startsWith("bundleresource:") || resURL.startsWith("bundle:")) {
            // Equinox/Felix/etc.
            try {
                URLConnection con = res.openConnection();
                Method m = con.getClass().getDeclaredMethod( "getLocalURL" );
                m.setAccessible(true);
                res = (URL)m.invoke(con);
            } catch ( Throwable e ) {
                // something must have changed in Equinox. fall through
                LOGGER.log(Level.FINE, "Failed to resolve bundleresource into a jar location",e);
            }
        }

        URLConnection con = res.openConnection();
        if (con instanceof JarURLConnection) {
            JarURLConnection jcon = (JarURLConnection) con;
            JarFile jarFile = jcon.getJarFile();
            if (jarFile!=null) {
                String n = jarFile.getName();
                if(n.length()>0) {// JDK6u10 needs this
                    return new File(n);
                } else {
                    // JDK6u10 apparently starts hiding the real jar file name,
                    // so this just keeps getting tricker and trickier...
                    try {
                        Field f = ZipFile.class.getDeclaredField("name");
                        f.setAccessible(true);
                        return new File((String) f.get(jarFile));
                    } catch (NoSuchFieldException e) {
                        LOGGER.log(Level.INFO, "Failed to obtain the local cache file name of "+clazz, e);
                    } catch (IllegalAccessException e) {
                        LOGGER.log(Level.INFO, "Failed to obtain the local cache file name of "+clazz, e);
                    }
                }
            }
        }

        throw new IllegalArgumentException(originalURL + " - " + resURL);
    }

    public static File jarFile(URL resource) throws IOException {
        return fromJarUrlToFile(resource.toExternalForm());
    }

    private static File fromJarUrlToFile(String resURL) throws MalformedURLException {
        resURL = resURL.substring(resURL.indexOf(':')+1, resURL.lastIndexOf('!')); // cut off "scheme:" and the file name portion
        return new File(decode(new URL(resURL).getPath()));
    }

    /**
     * Decode '%HH'.
     */
    private static String decode(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for( int i=0; i<s.length();i++ ) {
            char ch = s.charAt(i);
            if(ch=='%') {
                baos.write(hexToInt(s.charAt(i+1))*16 + hexToInt(s.charAt(i+2)));
                i+=2;
                continue;
            }
            baos.write(ch);
        }
        try {
            return new String(baos.toByteArray(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e); // impossible
        }
    }

    private static int hexToInt(int ch) {
        return Character.getNumericValue(ch);
    }

    private static final Logger LOGGER = Logger.getLogger(Which.class.getName());
}
