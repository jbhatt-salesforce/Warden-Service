/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.client.WardenHttpClient.RequestType;
import com.salesforce.dva.warden.client.WardenService.EndpointService;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.WardenUser;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
class UserService extends EndpointService {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final String REQUESTURL = "/user";

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new PolicyService object.
     *
     * @param  client  DOCUMENT ME!
     */
    UserService(WardenHttpClient client) {
        super(client);
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<WardenUser> getUsers() throws IOException {
        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<WardenUser> getUserById(String userName) throws IOException {
        String requestUrl = REQUESTURL + "/" + userName;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Policy> getPoliciesForUser(String userName) throws IOException {
        String requestUrl = REQUESTURL + "/" + userName + "/policy";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Infraction> getInfractionsForUserAndPolicy(String userName, BigInteger policyId) throws IOException {
        String requestUrl = REQUESTURL + "/" + userName + "/policy/" + policyId.toString() + "/infraction";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Infraction> getInfractionsForUser(String userName) throws IOException {
        String requestUrl = REQUESTURL + "/" + userName + "/infraction";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Map<Long, Double>> getMetricForUserAndPolicy(String userName, BigInteger policyId, Long start, Long end) throws IOException {
        String requestUrl = REQUESTURL + "/" + userName + "/policy" + policyId.toString() + "/metric?start=" + start + "&end=" + end;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Infraction> getSuspensionsForUser(String userName) throws IOException {
        String requestUrl = REQUESTURL + "/" + userName + "/suspension";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Infraction> getSuspensionForUser(String userName, BigInteger suspensionId) throws IOException {
        String requestUrl = REQUESTURL + "/" + userName + "/suspension/" + suspensionId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
