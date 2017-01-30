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

package com.salesforce.dva.argus.service;

import com.salesforce.dva.argus.entity.Infraction;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.entity.Policy;
import com.salesforce.dva.argus.entity.SuspensionLevel;
import java.math.BigInteger;
import java.util.List;

/**
 * Provides methods specific to Warden as a Service.
 *
 * @author         Ruofan Zhang (rzhang@salesforce.com)    
 * @author         Tom Valine (tvaline@salesforce.com)    
 */
public interface WaaSService {

    
    public Infraction getInfraction(BigInteger infractionId);
    
    public Infraction updateInfraction(Infraction infraction);
    
    public void deleteInfraction(BigInteger infractionId);

    public Policy getPolicy(BigInteger policyId);
    
    public Policy updatePolicy(Policy policy);
    
    public void deletePolicy(BigInteger policyId);

    public SuspensionLevel updateSuspensionLevel(SuspensionLevel toCreate);

    public SuspensionLevel getSuspensionLevelForPolicy(BigInteger id, BigInteger levelId);

    public void deleteSuspensionLevel(BigInteger id);

    public List<Infraction> getInfractionsByUserName(String userName);

    public List<Infraction> getInfractionsByPolicyAndUserName(BigInteger id, String userName);

    public List<Metric> getMetrics(BigInteger id, String userName, String start, String end);

    public List<Policy> getPoliciesForUserName(String userName);

    public List<Infraction> getSuspensionsByUserName(String userName);

    public Policy getPolicyByNameAndService(String name, String service);

    public List<Policy> getPolicies();

    public List<Infraction> getInfractionsForPolicy(BigInteger id);

    public void updateMetric(BigInteger policyId, String userName, Double value);
    /**
     * Helper method to construct a metric expression from the policy ID and userName.
     * 
     * @param policyId The policy ID.  Cannot be null.
     * @param userName The userName. Cannot be null or empty.
     * @param start The optional start time.  Can be relative or absolute timestamp.  If not specified it defaults to '-1d'.
     * @param end The optional end time.  Can be relative or absolute timestamp.  If not specified it defaults to '-0d'.
     * @return The corresponding metric query expression.
     */
    public String constructMetricExpression(BigInteger policyId, String userName, String start, String end);

    /**
     * Helper method to construct the metric name given the policy ID and userName.
     * @param policyId The policy ID.  Cannot be null.
     * @param userName The userName.  Cannot be null or empty.
     * @return The corresponding metric name.
     */
    public String constructMetricName(BigInteger policyId, String userName);

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
