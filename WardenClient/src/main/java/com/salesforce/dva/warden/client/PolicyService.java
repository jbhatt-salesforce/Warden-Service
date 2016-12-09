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
import jersey.repackaged.com.google.common.base.Objects;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;

/**
 * Provides methods to manipulate policy objects.
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
     * @param  client  The HTTP client to use. Cannot be null.
     */
    PolicyService(WardenHttpClient client) {
        super(client);
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Creates new policies.
     *
     * @param   policies  The policies to create. Cannot be null, but may be empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> createPolicies(List<Policy> policies) throws IOException {
        requireThat(policies != null, "Policies cannot be null.");

        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.POST, requestUrl, policies);
    }

    /**
     * Creates suspension levels for a policy.
     *
     * @param   policyId          The policy ID. Cannot be null.
     * @param   suspensionLevels  The suspension levels to create. Cannot be null, but may be empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<SuspensionLevel> createSuspensionLevels(BigInteger policyId, List<SuspensionLevel> suspensionLevels) throws IOException {
        requireThat(policyId != null, "Policy ID cannot be null.");
        requireThat(suspensionLevels != null, "Suspension levels cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level";

        return getClient().executeHttpRequest(RequestType.POST, requestUrl, suspensionLevels);
    }

    /**
     * Deletes policies.
     *
     * @param   policyIds  The IDs of the policies to delete. Cannot be null, but may be empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    /* use a Set<BigInteger> for input */
    WardenResponse<Policy> deletePolicies(Set<BigInteger> policyIds) throws IOException {
        requireThat(policyIds != null, "Policy IDs cannot be null.");

        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, policyIds);
    }

    /**
     * Deletes a policy.
     *
     * @param   policyId  The ID of the policy to delete. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> deletePolicy(BigInteger policyId) throws IOException {
        requireThat(policyId != null, "Policy ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString();

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    /**
     * Deletes a suspension level for a policy.
     *
     * @param   policyId           The ID of the policy. Cannot be null.
     * @param   suspensionLevelId  The ID of the suspension level to delete. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<SuspensionLevel> deleteSuspensionLevel(BigInteger policyId, BigInteger suspensionLevelId) throws IOException {
        requireThat(policyId != null, "Policy ID cannot be null.");
        requireThat(suspensionLevelId != null, "The suspension level ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level/" + suspensionLevelId.toString();

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    /**
     * Deletes all suspension levels for a policy.
     *
     * @param   policyId  The ID of the policy. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<SuspensionLevel> deleteSuspensionLevels(BigInteger policyId) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level";

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    /**
     * Deletes all suspensions for a policy.
     *
     * @param   policyId  The ID of the policy. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> deleteSuspensions(BigInteger policyId) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/suspension";

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    /**
     * Deletes suspensions for a specify user and policy combination.
     *
     * @param   policyId  The ID of the policy. Cannot be null.
     * @param   username  The username. Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> deleteSuspensionsForUserAndPolicy(BigInteger policyId, String username) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/user/" + username + "/suspension";

        return getClient().executeHttpRequest(RequestType.DELETE, requestUrl, null);
    }

    /**
     * Retrieves an infraction.
     *
     * @param   policyId      The policy ID. Cannot be null.
     * @param   infractionId  The infraction ID. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getInfraction(BigInteger policyId, BigInteger infractionId) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");
        requireThat(infractionId != null, "The infraction ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/infraction/" + infractionId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Retrieves infractions for a policy.
     *
     * @param   policyId  The ID of the policy. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getInfractions(BigInteger policyId) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/infraction";

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
    WardenResponse<Map<Long, Double>> getMetricForUserAndPolicy(BigInteger policyId, String username, Long start, Long end) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");
        requireThat(start != null && end == null ? start < System.currentTimeMillis() : true, "Start time cannot be null or occur in the future.");
        requireThat(start != null && end == null ? true : start <= end, "The start time must occur on or before the end time.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/user/" + username + "/metric?start=" + start + "&end=" + end;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns all policies.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> getPolicies() throws IOException {
        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Returns a policy for the given ID.
     *
     * @param   policyId  The policy ID. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> getPolicy(BigInteger policyId) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Retrieves a policy for the given service and policy name.
     *
     * @param   serviceName  The service name. Cannot be null or empty.
     * @param   policyName   The policy name. Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> getPolicy(String serviceName, String policyName) throws IOException {
        requireThat(serviceName != null && !serviceName.isEmpty(), "The service name cannot be null or empty.");
        requireThat(policyName != null && !policyName.isEmpty(), "The policy name cannot be null or empty.");

        String requestUrl = REQUESTURL + "?serviceName=" + serviceName + "&policyName=" + policyName;

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Retrieves a specific suspension level for a given policy.
     *
     * @param   policyId           The policy ID. Cannot be null.
     * @param   suspensionLevelId  The suspension level ID. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<SuspensionLevel> getSuspensionLevel(BigInteger policyId, BigInteger suspensionLevelId) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");
        requireThat(suspensionLevelId != null, "The suspension level ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level/" + suspensionLevelId.toString();

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Retrieves the suspension levels for a policy.
     *
     * @param   policyId  The policy ID. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<SuspensionLevel> getSuspensionLevels(BigInteger policyId) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Retrieves suspensions for a specific policy.
     *
     * @param   policyId  The policy ID. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getSuspensions(BigInteger policyId) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/suspension";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Retrieves suspensions for a specific user and policy.
     *
     * @param   policyId  The policy ID. Cannot be null.
     * @param   username  The username. Cannot be null or empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Infraction> getSuspensionsForUserAndPolicy(BigInteger policyId, String username) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/user/" + username + "/suspension";

        return getClient().executeHttpRequest(RequestType.GET, requestUrl, null);
    }

    /**
     * Updates usage metrics for a user and policy.
     *
     * @param   policyId  The policy ID. Cannot be null.
     * @param   username  The username. Cannot be null or empty.
     * @param   values    The timestamp and values of the usage metrics. Cannot be null, but may be empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse updateMetricsForUserAndPolicy(BigInteger policyId, String username, Map<Long, Double> values) throws IOException {
        requireThat(policyId != null, "The policy ID cannot be null.");
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");
        requireThat(values != null, "Values cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/user/" + username + "/metric";

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, values);
    }

    /**
     * Updates policies.
     *
     * @param   policies  The policies to update. Cannot be null, but may be empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> updatePolicies(List<Policy> policies) throws IOException {
        requireThat(policies != null, "Polcies cannot be null.");

        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, policies);
    }

    /**
     * Updates a policy.
     *
     * @param   policyId  The policy ID. Cannot be null.
     * @param   policy    The updated policy. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Policy> updatePolicy(BigInteger policyId, Policy policy) throws IOException {
        requireThat(policyId != null, "Policy ID cannot be null.");
        requireThat(policy != null, "The policy cannot be null.");
        requireThat(Objects.equal(policyId, policy.getId()), "Updates to the policy ID field are not supported.");

        String requestUrl = REQUESTURL + "/" + policyId.toString();

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, policy);
    }

    /**
     * Updates the suspension level for a policy.
     *
     * @param   policyId           The policy ID. Cannot be null.
     * @param   suspensionLevelId  The suspension level ID. Cannot be null.
     * @param   suspensionLevel    The updated suspension level. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<SuspensionLevel> updateSuspensionLevel(BigInteger policyId, BigInteger suspensionLevelId, SuspensionLevel suspensionLevel)
    throws IOException {
        requireThat(policyId != null, "Policy ID cannot be null.");
        requireThat(suspensionLevelId != null, "Suspension level ID cannot be null.");
        requireThat(Objects.equal(suspensionLevelId, suspensionLevel.getId()), "Updates to the level ID field are not supported.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level/" + suspensionLevelId.toString();

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, suspensionLevel);
    }

    /**
     * Updates the suspension levels for a policy.
     *
     * @param   policyId          The policy ID. Cannot be null.
     * @param   suspensionLevels  The suspension levels to update. Cannot be null, but may be empty.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<SuspensionLevel> updateSuspensionLevels(BigInteger policyId, List<SuspensionLevel> suspensionLevels) throws IOException {
        requireThat(policyId != null, "Policy ID cannot be null.");
        requireThat(suspensionLevels != null, "Suspension levels cannot be null.");

        String requestUrl = REQUESTURL + "/" + policyId.toString() + "/level";

        return getClient().executeHttpRequest(RequestType.PUT, requestUrl, suspensionLevels);
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
