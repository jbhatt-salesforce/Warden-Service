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

import com.salesforce.dva.warden.client.WardenHttpClient.RequestType;
import com.salesforce.dva.warden.client.WardenService.EndpointService;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.SuspensionLevel;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
final class PolicyService extends EndpointService {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final String REQUESTURL = "/policy";

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new PolicyService object.
     *
     * @param  client  DOCUMENT ME!
     */
    PolicyService(WardenHttpClient client) {
        super(client);
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * DOCUMENT ME!
     *
     * @param   policies  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Policy> createPolicies(List<Policy> policies) throws IOException {
        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.POST, requestUrl, policies);
    }

    WardenResponse<SuspensionLevel> createSuspensionLevels(BigInteger policyId, List<SuspensionLevel> suspensionLevels) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level";

        return getClient().executeHttpRequest(RequestType.POST, requestUrl, suspensionLevels);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   policyIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    /* use a Set<BigInteger> for input */
    WardenResponse<Policy> deletePolicies(BigInteger[] policyIds) throws IOException {
        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, policyIds);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   policyId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Policy> deletePolicy(BigInteger policyId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString();

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    WardenResponse<SuspensionLevel> deleteSuspensionLevel(BigInteger policyId, BigInteger suspensionLevelId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level/" + suspensionLevelId.toString();

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    WardenResponse<SuspensionLevel> deleteSuspensionLevels(BigInteger policyId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level";

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    WardenResponse<Infraction> deleteSuspensions(BigInteger policyId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/suspension";

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    WardenResponse<Infraction> deleteSuspensionsForUserAndPolicy(BigInteger policyId, String userName) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/user/" + userName + "/suspension";

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    WardenResponse<Infraction> getInfraction(BigInteger policyId, BigInteger infractionId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/infraction/" + infractionId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    // ======================= Infractions CRUD ====================================
    WardenResponse<Infraction> getInfractions(BigInteger policyId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/infraction";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    // ============================ Metrics given a PolicyId and a UserId CRUD=================================
    WardenResponse<Map<Long, Double>> getMetricForUserAndPolicy(BigInteger policyId, String userName, Long start, Long end) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/user/" + userName + "/metric?start=" + start + "&end=" + end;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Policy> getPolicies() throws IOException {
        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   policyId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Policy> getPolicy(BigInteger policyId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    WardenResponse<Policy> getPolicy(String serviceName, String policyName) throws IOException {
        String requestUrl = REQUESTURL + "?serviceName=" + serviceName + "&policyName=" + policyName;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    // ========================One SuspensionLevel CRUD==========================
    WardenResponse<SuspensionLevel> getSuspensionLevel(BigInteger policyId, BigInteger suspensionLevelId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level/" + suspensionLevelId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    // ======================= SuspensionLevels CRUD=========================
    WardenResponse<SuspensionLevel> getSuspensionLevels(BigInteger policyId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    // ========================= Suspensions given a PolicyId CRUD ====================================
    WardenResponse<Infraction> getSuspensions(BigInteger policyId) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/suspension";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    // =========================== Suspensions given a PolicyId and a UserId CRUD ===========================
    WardenResponse<Infraction> getSuspensionsForUserAndPolicy(BigInteger policyId, String userName) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/user/" + userName + "/suspension";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    WardenResponse updateMetricsForUserAndPolicy(BigInteger policyId, String userName, Map<Long, Double> values) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/user/" + userName + "/metric";

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, values);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   policies  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Policy> updatePolicies(List<Policy> policies) throws IOException {
        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, policies);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   policyId  DOCUMENT ME!
     * @param   policy    DOCUMENT ME!
     *
     * @return  WardenResponse<Policy>
     *
     * @throws  IOException  DOCUMENT ME!
     */
    WardenResponse<Policy> updatePolicy(BigInteger policyId, Policy policy) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString();
        /*if (policyId != policy.getId()){
         *  throw new IOException("The input argument policyId, does not match with the policyId of the input argument policy");}*/

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, policy);
    }

    WardenResponse<SuspensionLevel> updateSuspensionLevel(BigInteger policyId, BigInteger suspensionLevelId, SuspensionLevel suspensionLevel)
    throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level/" + suspensionLevelId.toString();

        /*if ( suspensionLevelId != suspensionLevel.getId()) {
         *  throw new IOException("The input argument suspensionLevelId, does not match with the suspensionLevelId of the input argument
         * suspensionLevel");}*/
        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, suspensionLevel);
    }

    WardenResponse<SuspensionLevel> updateSuspensionLevels(BigInteger policyId, List<SuspensionLevel> suspensionLevels) throws IOException {
        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level";

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, suspensionLevels);
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
