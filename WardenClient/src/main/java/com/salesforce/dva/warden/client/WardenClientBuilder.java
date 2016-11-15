/* Copyright (c) 2015-2016, Salesforce.com, Inc.
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *   
 *      Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 *      Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *      Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.WardenClient;
import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class WardenClientBuilder {

    //~ Instance fields ******************************************************************************************************************************

    private String endpoint;
    private String username;
    private String password;

    //~ Constructors *********************************************************************************************************************************

    /** Creates a new WardenClientBuilder object. */
    public WardenClientBuilder() { }

    //~ Methods **************************************************************************************************************************************

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public WardenClient build() throws IOException {
        return new DefaultWardenClient(endpoint, username, password);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   endpoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WardenClientBuilder forEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   password  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WardenClientBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   username  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WardenClientBuilder withUsername(String username) {
        this.username = username;
        return this;
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
