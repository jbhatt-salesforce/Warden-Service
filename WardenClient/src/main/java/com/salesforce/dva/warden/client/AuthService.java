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

import com.salesforce.dva.warden.client.WardenService.EndpointService;
import com.salesforce.dva.warden.dto.Credentials;
import java.io.IOException;

import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;

/**
 * Provides methods to authenticate a user.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 * @author  Tom Valine (tvaline@salesforce.com)
 */
class AuthService extends EndpointService {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final String REQUESTURL = "/auth";

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new AuthService object.
     *
     * @param  client  The HTTP client implementation to use. Cannot be null.
     */
    AuthService(WardenHttpClient client) {
        super(client);
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Logs into the web services.
     *
     * @param   username  The username. Cannot be null or empty.
     * @param   password  The password. Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse login(String username, String password) throws IOException {
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");
        requireThat(password != null && !password.isEmpty(), "Password cannot be null or empty.");

        String requestUrl = REQUESTURL + "/login";
        Credentials creds = new Credentials();

        creds.setPassword(password);
        creds.setUsername(username);
        return getClient().executeHttpRequest(WardenHttpClient.RequestType.POST, requestUrl, creds);
    }

    /**
     * Logs out of the web services.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse logout() throws IOException {
        String requestUrl = REQUESTURL + "/logout";

        return getClient().executeHttpRequest(WardenHttpClient.RequestType.GET, requestUrl, null);
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
