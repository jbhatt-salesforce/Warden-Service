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

import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;
import com.salesforce.dva.warden.client.WardenHttpClient.RequestType;
import com.salesforce.dva.warden.client.WardenService.EndpointService;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.WardenUser;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

/**
 * Provides methods to access data for specific users.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
class UserService extends EndpointService {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final String REQUESTURL = "/user";

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new UserService object.
     *
     * @param  client  The HTTP client to use.  Cannot be null.
     */
    UserService(WardenHttpClient client) {
        super(client);
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Retrieves infractions for a specific user across all policies.
     *
     * @param   username  The username.  Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getInfractionsForUser(String username) throws IOException {
        requireThat(username!=null && !username.isEmpty(), "Username cannot be null or empty.");
        String requestUrl = REQUESTURL + "/" + username + "/infraction";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns all infractions for a user and policy combination.
     *
     * @param   username  The username.  Cannot be null.
     * @param   policyId  The policy ID.  Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getInfractionsForUserAndPolicy(String username, BigInteger policyId) throws IOException {
        requireThat(username!=null && !username.isEmpty(), "Username cannot be null or empty.");
        requireThat(policyId != null, "Policy ID cannot be null.");
        String requestUrl = REQUESTURL + "/" + username + "/policy/" + policyId.toString() + "/infraction";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Return usage metric data for a policy and username combination.
     *
     * @param   policyId  The policy ID. Cannot be null.
     * @param   username  The username. Cannot be null or empty.
     * @param   start     The start of the time range. Cannot be null and must occur before the end time.
     * @param   end       The end of the time range. If null defaults to current timestamp, otherwise must occur on or after the start time.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Map<Long, Double>> getMetricForUserAndPolicy(String username, BigInteger policyId, Long start, Long end) throws IOException {
        requireThat(username!=null && !username.isEmpty(), "Username cannot be null or empty.");
        requireThat(policyId != null, "Policy ID cannot be null.");
        requireThat(start != null && end == null ? start < System.currentTimeMillis() : true, "Start time cannot be null or occur in the future.");
        requireThat(start != null && end == null ? true : start <= end, "The start time must occur on or before the end time.");
        String requestUrl = REQUESTURL + "/" + username + "/policy" + policyId.toString() + "/metric?start=" + start + "&end=" + end;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns policies for a user.
     *
     * @param   username  The username.  Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> getPoliciesForUser(String username) throws IOException {
        requireThat(username!=null && !username.isEmpty(), "Username cannot be null or empty.");
        String requestUrl = REQUESTURL + "/" + username + "/policy";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns the suspension specific to a user.
     *
     * @param   username      The username.  Cannot be null or empty.
     * @param   suspensionId  The suspension ID.  Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getSuspensionForUser(String username, BigInteger suspensionId) throws IOException {
        requireThat(username!=null && !username.isEmpty(), "Username cannot be null or empty.");
        requireThat(suspensionId != null, "Suspension ID cannot be null.");
        String requestUrl = REQUESTURL + "/" + username + "/suspension/" + suspensionId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns all suspensions for a specific user.
     *
     * @param   username      The username.  Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getSuspensionsForUser(String username) throws IOException {
        requireThat(username!=null && !username.isEmpty(), "Username cannot be null or empty.");
        String requestUrl = REQUESTURL + "/" + username + "/suspension";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns user detail information.
     *
     * @param   username      The username.  Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<WardenUser> getUserByUsername(String username) throws IOException {
        requireThat(username!=null && !username.isEmpty(), "Username cannot be null or empty.");
        String requestUrl = REQUESTURL + "/" + username;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns the list of warden users.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<WardenUser> getUsers() throws IOException {
        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
