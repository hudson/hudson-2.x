/*
 * The MIT License
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi
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

import com.trilead.ssh2.crypto.Base64;
import hudson.PluginWrapper;
import hudson.Util;
import hudson.Extension;
import hudson.node_monitors.ArchitectureMonitor.DescriptorImpl;
import hudson.util.Secret;
import static hudson.util.TimeUnit2.DAYS;
import net.sf.json.JSONObject;
import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.DataInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class UsageStatistics extends PageDecorator {

    private final String keyImage;
    /**
     * Lazily computed {@link PublicKey} representation of {@link #keyImage}.
     */
    private volatile transient PublicKey key;
    /**
     * When was the last time we asked a browser to send the usage stats for us?
     */
    private volatile transient long lastAttempt = -1;
    
    /**
     * Public key (Hex encoded) to encrypt the usage statistics
     */
    private static final String DEFAULT_PUBLIC_KEY = "30819f300d06092a864886f70d010101050003818d00308189028181008e68beffebd5a213d4b47b29d611221c8cd145a865290ceac6769395cdaf98a784c11f4880548d119b3faffb79b51d06c7c783ee2d897b34c3e27010a06e9798d5b4effa4cafb74a90bf8e48099f859ce040d766eeba7d9f0d02c653d6b6a7f317e5734c03befcc3f87342257fe8e4b2f31aeefba5a60356fdedcf62169561150203010001";

    public UsageStatistics() {
        this(DEFAULT_PUBLIC_KEY);
    }

    /**
     * Creates an instance with a specific public key image.
     */
    public UsageStatistics(String keyImage) {
        super(UsageStatistics.class);
        this.keyImage = keyImage;
        load();
    }

    /**
     * Returns true if it's time for us to check for new version.
     */
    public boolean isDue() {
        // user opted out. no data collection.
        if (!Hudson.getInstance().isUsageStatisticsCollected() || DISABLED) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - lastAttempt > DAY) {
            lastAttempt = now;
            return true;
        }
        return false;
    }

    private Cipher getCipher() {
        try {
            if (key == null) {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                key = keyFactory.generatePublic(new X509EncodedKeySpec(Util.fromHexString(keyImage)));
            }

            Cipher cipher = Secret.getCipher("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;
        } catch (GeneralSecurityException e) {
            throw new Error(e); // impossible
        }
    }

    /**
     * Gets the encrypted usage stat data to be sent to the Hudson server.
     */
    public String getStatData() throws IOException {
        Hudson h = Hudson.getInstance();

        JSONObject o = new JSONObject();
        o.put("stat", 1);
        o.put("install", Util.getDigestOf(h.getSecretKey()));
        o.put("version", Hudson.VERSION);

        List<JSONObject> nodes = new ArrayList<JSONObject>();
        for (Computer c : h.getComputers()) {
            JSONObject n = new JSONObject();
            if (c.getNode() == h) {
                n.put("master", true);
                n.put("jvm-vendor", System.getProperty("java.vm.vendor"));
                n.put("jvm-version", System.getProperty("java.version"));
            }
            n.put("executors", c.getNumExecutors());
            DescriptorImpl descriptor = h.getDescriptorByType(DescriptorImpl.class);
            n.put("os", descriptor.get(c));
            nodes.add(n);
        }
        o.put("nodes", nodes);

        List<JSONObject> plugins = new ArrayList<JSONObject>();
        for (PluginWrapper pw : h.getPluginManager().getPlugins()) {
            if (!pw.isActive()) {
                continue;   // treat disabled plugins as if they are uninstalled
            }
            JSONObject p = new JSONObject();
            p.put("name", pw.getShortName());
            p.put("version", pw.getVersion());
            plugins.add(p);
        }
        o.put("plugins", plugins);

        JSONObject jobs = new JSONObject();
        List<TopLevelItem> items = h.getItems();
        for (TopLevelItemDescriptor d : Items.all()) {
            int cnt = 0;
            for (TopLevelItem item : items) {
                if (item.getDescriptor() == d) {
                    cnt++;
                }
            }
            jobs.put(d.getJsonSafeClassName(), cnt);
        }
        o.put("jobs", jobs);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // json -> UTF-8 encode -> gzip -> encrypt -> base64 -> string
            OutputStreamWriter w = new OutputStreamWriter(new GZIPOutputStream(new CombinedCipherOutputStream(baos, getCipher(), "AES")), "UTF-8");
            o.write(w);
            w.close();

            return new String(Base64.encode(baos.toByteArray()));
        } catch (GeneralSecurityException e) {
            throw new Error(e); // impossible
        }
    }

    /**
     * Assymetric cipher is slow and in case of Sun RSA implementation it can only encyrypt the first block.
     *
     * So first create a symmetric key, then place this key in the beginning of the stream by encrypting it
     * with the assymetric cipher. The rest of the stream will be encrypted by a symmetric cipher.
     */
    public static final class CombinedCipherOutputStream extends FilterOutputStream {

        public CombinedCipherOutputStream(OutputStream out, Cipher asym, String algorithm) throws IOException, GeneralSecurityException {
            super(out);

            // create a new symmetric cipher key used for this stream
            SecretKey symKey = KeyGenerator.getInstance(algorithm).generateKey();

            // place the symmetric key by encrypting it with asymmetric cipher
            out.write(asym.doFinal(symKey.getEncoded()));

            // the rest of the data will be encrypted by this symmetric cipher
            Cipher sym = Secret.getCipher(algorithm);
            sym.init(Cipher.ENCRYPT_MODE, symKey);
            super.out = new CipherOutputStream(out, sym);
        }
    }

    /**
     * The opposite of the {@link CombinedCipherOutputStream}.
     */
    public static final class CombinedCipherInputStream extends FilterInputStream {

        /**
         * @param keyLength
         *      Block size of the asymmetric cipher, in bits. I thought I can get it from {@code asym.getBlockSize()}
         *      but that doesn't work with Sun's implementation.
         */
        public CombinedCipherInputStream(InputStream in, Cipher asym, String algorithm, int keyLength) throws IOException, GeneralSecurityException {
            super(in);

            // first read the symmetric key cipher
            byte[] symKeyBytes = new byte[keyLength / 8];
            new DataInputStream(in).readFully(symKeyBytes);
            SecretKey symKey = new SecretKeySpec(asym.doFinal(symKeyBytes), algorithm);

            // the rest of the data will be decrypted by this symmetric cipher
            Cipher sym = Secret.getCipher(algorithm);
            sym.init(Cipher.DECRYPT_MODE, symKey);
            super.in = new CipherInputStream(in, sym);
        }
    }
    private static final long DAY = DAYS.toMillis(1);
    public static boolean DISABLED = Boolean.getBoolean(UsageStatistics.class.getName() + ".disabled");
}
