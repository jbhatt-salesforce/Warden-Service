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

import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;

/**
 * Used to build new instances of the client.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class WardenClientBuilder {

    //~ Instance fields ******************************************************************************************************************************

    private String _endpoint;
    private String _username;
    private String _password;

    //~ Constructors *********************************************************************************************************************************

    /** Creates a new WardenClientBuilder object. */
    public WardenClientBuilder() { }

    //~ Methods **************************************************************************************************************************************

    /**
     * Creates a new instance of the client using the configuration specified by the builder.
     *
     * @return  The configured client.
     *
     * @throws  IOException  If the Warden web service endpoint is not reachable or invalid.
     */
    public WardenClient build() throws IOException {
        return new DefaultWardenClient(_endpoint, _username, _password);
    }

    /**
     * Specifies the Warden web service endpoint to connect to.
     *
     * @param   endpoint  The URL of the web service endpoint. Cannot be null and must be a well formed URL.
     *
     * @return  The updated instance of the builder.
     */
    public WardenClientBuilder forEndpoint(String endpoint) {
        requireThat(endpoint != null && !endpoint.isEmpty(), "Invalid endpoint.");
        _endpoint = endpoint;
        return this;
    }

    /**
     * Specifies the password with which to connect to the web services.
     *
     * @param   password  The password. Cannot be null or empty.
     *
     * @return  The updated instance of the builder.
     */
    public WardenClientBuilder withPassword(String password) {
        requireThat(password != null && !password.isEmpty(), "Password cannot be null or empty.");
        _password = password;
        return this;
    }

    /**
     * Specifies the username with which to connect to the web services.
     *
     * @param   username  The username. Cannot be null or empty.
     *
     * @return  The updated instance of the builder.
     */
    public WardenClientBuilder withUsername(String username) {
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");
        _username = username;
        return this;
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
