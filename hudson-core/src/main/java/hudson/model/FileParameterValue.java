/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
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

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import hudson.tasks.BuildWrapper;
import hudson.Launcher;
import hudson.FilePath;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.servlet.ServletException;

/**
 * {@link ParameterValue} for {@link FileParameterDefinition}.
 *
 * <h2>Persistence</h2>
 * <p>
 * {@link DiskFileItem} is persistable via serialization,
 * (although the data may get very large in XML) so this object
 * as a whole is persistable.
 *
 * @author Kohsuke Kawaguchi
 */
public class FileParameterValue extends ParameterValue {
    private FileItem file;

    /**
     * The name of the originally uploaded file.
     */
    private final String originalFileName;

    private String location;

    @DataBoundConstructor
    public FileParameterValue(String name, FileItem file) {
        this(name, file, FilenameUtils.getName(file.getName()));
    }

    public FileParameterValue(String name, File file, String originalFileName) {
        this(name, new FileItemImpl(file), originalFileName);
    }

    private FileParameterValue(String name, FileItem file, String originalFileName) {
        super(name);
        this.file = file;
        this.originalFileName = originalFileName;
    }

    // post initialization hook
    /*package*/ void setLocation(String location) {
        this.location = location;
    }

    /**
     * Get the name of the originally uploaded file. If this
     * {@link FileParameterValue} was created prior to 1.362, this method will
     * return {@code null}.
     *
     * @return the name of the originally uploaded file
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    @Override
    public BuildWrapper createBuildWrapper(AbstractBuild<?,?> build) {
        return new BuildWrapper() {
            @Override
            public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
            	if (!StringUtils.isEmpty(file.getName())) {
            	    listener.getLogger().println("Copying file to "+location);
                    FilePath locationFilePath = build.getWorkspace().child(location);
                    locationFilePath.getParent().mkdirs();
            	    locationFilePath.copyFrom(file);
            	    file = null;
                    locationFilePath.copyTo(new FilePath(getLocationUnderBuild(build)));
            	}
                return new Environment() {};
            }
        };
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		return result;
	}

	/**
	 * In practice this will always be false, since location should be unique.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileParameterValue other = (FileParameterValue) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

    @Override
    public String getShortDescription() {
    	return "(FileParameterValue) " + getName() + "='" + originalFileName + "'";
    }

    /**
     * Serve this file parameter in response to a {@link StaplerRequest}.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doDynamic(StaplerRequest request, StaplerResponse response) throws ServletException, IOException {
        if (("/" + originalFileName).equals(request.getRestOfPath())) {
            AbstractBuild build = (AbstractBuild)request.findAncestor(AbstractBuild.class).getObject();
            File fileParameter = getLocationUnderBuild(build);
            if (fileParameter.isFile()) {
                response.serveFile(request, fileParameter.toURI().toURL());
            }
        }
    }

    /**
     * Get the location under the build directory to store the file parameter.
     *
     * @param build the build
     * @return the location to store the file parameter
     */
    private File getLocationUnderBuild(AbstractBuild build) {
        return new File(build.getRootDir(), "fileParameters/" + location);
    }

    /**
     * Default implementation from {@link File}.
     */
    public static final class FileItemImpl implements FileItem {
        private final File file;

        public FileItemImpl(File file) {
            if (file == null) {
                throw new NullPointerException("file");
            }
            this.file = file;
        }

        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        public String getContentType() {
            return null;
        }

        public String getName() {
            return file.getName();
        }

        public boolean isInMemory() {
            return false;
        }

        public long getSize() {
            return file.length();
        }

        public byte[] get() {
            try {
                return IOUtils.toByteArray(new FileInputStream(file));
            } catch (IOException e) {
                throw new Error(e);
            }
        }

        public String getString(String encoding) throws UnsupportedEncodingException {
            return new String(get(), encoding);
        }

        public String getString() {
            return new String(get());
        }

        public void write(File to) throws Exception {
            new FilePath(file).copyTo(new FilePath(to));
        }

        public void delete() {
            file.delete();
        }

        public String getFieldName() {
            return null;
        }

        public void setFieldName(String name) {
        }

        public boolean isFormField() {
            return false;
        }

        public void setFormField(boolean state) {
        }

        public OutputStream getOutputStream() throws IOException {
            return new FileOutputStream(file);
        }
    }
}
