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
package com.salesforce.dva.warden.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Policy usage metric information.
 *
 * @author Tom Valine (tvaline@salesforce.com)
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metric extends Base {

    //~ Static fields/initializers *******************************************************************************************************************
    private static final long serialVersionUID = 1L;

    //~ Instance fields ******************************************************************************************************************************
    private Map<Long, Double> datapoints = new TreeMap<>();
    private BigInteger policyId;
    private String username;

    //~ Methods **************************************************************************************************************************************
    @Override
    public Metric createExample() {
        Metric result = new Metric();

        result.datapoints.put(System.currentTimeMillis(), 1.0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Metric other = (Metric) obj;

        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.datapoints, other.datapoints)) {
            return false;
        }
        if (!Objects.equals(this.policyId, other.policyId)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the data points for the usage data.
     *
     * @return The data points for the usage data.
     */
    public Map<Long, Double> getDatapoints() {
        return datapoints;
    }

    /**
     * Returns the policy ID.
     *
     * @return The policy ID.
     */
    public BigInteger getPolicyId() {
        return policyId;
    }

    /**
     * Returns the username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 37 * hash + Objects.hashCode(this.datapoints);
        hash = 37 * hash + Objects.hashCode(this.policyId);
        hash = 37 * hash + Objects.hashCode(this.username);
        return hash;
    }

    /**
     * Sets the metric usage data points.
     *
     * @param datapoints The data points.
     */
    public void setDatapoints(Map<Long, Double> datapoints) {
        this.datapoints = datapoints;
    }

    /**
     * Sets the policy ID.
     *
     * @param policyId The policy ID.
     */
    public void setPolicyId(BigInteger policyId) {
        this.policyId = policyId;
    }

    /**
     * Sets the user name.
     *
     * @param username The user name.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Metric{" + "datapoints=" + datapoints + ", policyId=" + policyId + ", username=" + username + '}';
    }

}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
