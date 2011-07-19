/*******************************************************************************
 *
 * Copyright (c) 2008-2009 Yahoo! Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      
 *
 *******************************************************************************/ 

package hudson.security.csrf;

import hudson.Util;
import hudson.model.Descriptor;

/**
 * Describes global configuration for crumb issuers. Create subclasses to specify
 * additional global configuration for custom crumb issuers.
 * 
 * @author dty
 */
public abstract class CrumbIssuerDescriptor<T extends CrumbIssuer> extends Descriptor<CrumbIssuer> {

    private String crumbSalt;
    private String crumbRequestField;

    /**
     * Crumb issuers always take a salt and a request field name.
     *
     * @param salt Salt value
     * @param crumbRequestField Request parameter name containing crumb from previous response
     */
    protected CrumbIssuerDescriptor(String salt, String crumbRequestField) {
        setCrumbSalt(salt);
        setCrumbRequestField(crumbRequestField);
    }

    /**
     * Get the salt value.
     * @return
     */
    public String getCrumbSalt() {
        return crumbSalt;
    }

    /**
     * Set the salt value. Must not be null.
     * @param salt
     */
    public void setCrumbSalt(String salt) {
        if (Util.fixEmptyAndTrim(salt) == null) {
            crumbSalt = "hudson.crumb";
        } else {
            crumbSalt = salt;
        }
    }

    /**
     * Gets the request parameter name that contains the crumb generated from a
     * previous response.
     *
     * @return
     */
    public String getCrumbRequestField() {
        return crumbRequestField;
    }

    /**
     * Set the request parameter name. Must not be null.
     *
     * @param requestField
     */
    public void setCrumbRequestField(String requestField) {
        if (Util.fixEmptyAndTrim(requestField) == null) {
            crumbRequestField = ".crumb";
        } else {
            crumbRequestField = requestField;
        }
    }
}
