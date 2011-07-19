/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Winston Prakash
 *      
 *
 *******************************************************************************/ 

package hudson.security.captcha;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Extension point for adding Captcha Support to User Registration Page {@link CaptchaSupport}.
 *
 * <p>
 * This object can have an optional <tt>config.jelly</tt> to configure the Captcha Support
 * <p>
 * A default constructor is needed to create CaptchaSupport in
 * the default configuration.
 *
 * @author Winston Prakash
 * @since 2.0.1
 * @see CaptchaSupportDescriptor
 */
public abstract class CaptchaSupport extends AbstractDescribableImpl<CaptchaSupport> implements ExtensionPoint {
    /**
     * Returns all the registered {@link CaptchaSupport} descriptors.
     */
    public static DescriptorExtensionList<CaptchaSupport, Descriptor<CaptchaSupport>> all() {
        return Hudson.getInstance().<CaptchaSupport, Descriptor<CaptchaSupport>>getDescriptorList(CaptchaSupport.class);
    }
    
    abstract public  boolean validateCaptcha(String id, String text); 
    
    abstract public void generateImage(String id, OutputStream ios) throws IOException;

    public CaptchaSupportDescriptor getDescriptor() {
        return (CaptchaSupportDescriptor)super.getDescriptor();
    }
}
