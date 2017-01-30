/* Copyright (c) 2015-2017, Salesforce.com, Inc.
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

import java.io.IOException;
import java.math.BigInteger;
import com.salesforce.dva.warden.client.WardenHttpClient.RequestType;
import com.salesforce.dva.warden.client.WardenService.EndpointService;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Metric;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.User;
import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;

/**
 * Provides methods to access data for specific users.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
class UserService extends EndpointService {

    private static final String REQUESTURL = "/user";

    /**
     * Creates a new UserService object.
     *
     * @param  client  The HTTP client to use. Cannot be null.
     */
    UserService(WardenHttpClient client) {
        super(client);
    }

    /**
     * Retrieves infractions for a specific user across all policies.
     *
     * @param   userName  The userName. Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getInfractionsForUser(String userName) throws IOException {
        requireThat((userName != null) &&!userName.isEmpty(), "Username cannot be null or empty.");

        String requestUrl = REQUESTURL + "/" + userName + "/infraction";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns all infractions for a user and policy combination.
     *
     * @param   userName  The userName. Cannot be null.
     * @param   policyId  The policy ID. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getInfractionsForUserAndPolicy(String userName, BigInteger policyId) throws IOException {
        requireThat((userName != null) &&!userName.isEmpty(), "Username cannot be null or empty.");
        requireThat(policyId != null, "Policy ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + userName + "/policy/" + policyId.toString() + "/infraction";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Return usage metric data for a policy and userName combination.
     *
     * @param   userName  The userName. Cannot be null or empty.
     * @param   policyId  The policy ID. Cannot be null.
     * @param   start     The start of the time range. Cannot be null and must occur before the end time.
     * @param   end       The end of the time range. If null defaults to current timestamp, otherwise must occur on or after the start time.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Metric> getMetricForUserAndPolicy(String userName, BigInteger policyId, String start, String end) throws IOException {
        requireThat((userName != null) &&!userName.isEmpty(), "Username cannot be null or empty.");
        requireThat(policyId != null, "Policy ID cannot be null.");

        if (start == null) {
            start = "-30d";
        }

        if (end == null) {
            end = "-0d";
        }

        String requestUrl = REQUESTURL + "/" + userName + "/policy" + policyId.toString() + "/metric?start=" + start + "&end=" + end;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns policies for a user.
     *
     * @param   userName  The userName. Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> getPoliciesForUser(String userName) throws IOException {
        requireThat((userName != null) &&!userName.isEmpty(), "Username cannot be null or empty.");

        String requestUrl = REQUESTURL + "/" + userName + "/policy";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns the suspension specific to a user.
     *
     * @param   userName      The userName. Cannot be null or empty.
     * @param   suspensionId  The suspension ID. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getSuspensionForUser(String userName, BigInteger suspensionId) throws IOException {
        requireThat((userName != null) &&!userName.isEmpty(), "Username cannot be null or empty.");
        requireThat(suspensionId != null, "Suspension ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + userName + "/suspension/" + suspensionId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns all suspensions for a specific user.
     *
     * @param   userName  The userName. Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getSuspensionsForUser(String userName) throws IOException {
        requireThat((userName != null) &&!userName.isEmpty(), "Username cannot be null or empty.");

        String requestUrl = REQUESTURL + "/" + userName + "/suspension";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns user detail information.
     *
     * @param   userName  The userName. Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<User> getUserByUserName(String userName) throws IOException {
        requireThat((userName != null) &&!userName.isEmpty(), "Username cannot be null or empty.");

        String requestUrl = REQUESTURL + "/" + userName;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns the list of warden users.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<User> getUsers() throws IOException {
        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
