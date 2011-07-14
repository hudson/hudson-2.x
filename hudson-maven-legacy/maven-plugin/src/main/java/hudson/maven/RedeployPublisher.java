package hudson.maven;

import hudson.tasks.Publisher;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.RedeployPublisher
 */
public class RedeployPublisher extends org.eclipse.hudson.legacy.maven.plugin.RedeployPublisher {

    public RedeployPublisher(String id, String url, boolean uniqueVersion,
            boolean evenIfUnstable) {
        super(id, url, uniqueVersion, evenIfUnstable);
    }

    public static class DescriptorImpl extends org.eclipse.hudson.legacy.maven.plugin.RedeployPublisher.DescriptorImpl {

        public DescriptorImpl() {
        }

        /**
         * @deprecated as of 1.290 Use the default constructor.
         */
        protected DescriptorImpl(Class<? extends Publisher> clazz) {
            super(clazz);
        }
    }
}
