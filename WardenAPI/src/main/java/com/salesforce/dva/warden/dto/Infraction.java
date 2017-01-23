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

package com.salesforce.dva.warden.dto;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Infraction record.
 *
 * @author Jigna Bhatt (jbhatt@salesforce.com)
 */
@JsonTypeName("infraction")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Infraction extends Entity {

    private static final long serialVersionUID = 1L;
    private BigInteger policyId;
    private BigInteger userId;
    private String username;
    private Long infractionTimestamp;
    private Long expirationTimestamp;
    private Double value;

    @Override
    public Infraction createExample() {
        Infraction result = new Infraction();

        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date(1451606400000L));
        result.setModifiedById(BigInteger.ONE);
        result.setModifiedDate(new Date(1451606400000L));
        result.setPolicyId(BigInteger.ONE);
        result.setUserId(BigInteger.ONE);
        result.setUsername("hpotter");
        result.setInfractionTimestamp(1L);
        result.setExpirationTimestamp(10L);
        result.setValue(1.00);

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

        final Infraction other = (Infraction) obj;

        if (!super.equals(other)) {
            return false;
        }

        if (!Objects.equals(this.username, other.username)) {
            return false;
        }

        if (!Objects.equals(this.policyId, other.policyId)) {
            return false;
        }

        if (!Objects.equals(this.userId, other.userId)) {
            return false;
        }

        if (!Objects.equals(this.infractionTimestamp, other.infractionTimestamp)) {
            return false;
        }

        if (!Objects.equals(this.expirationTimestamp, other.expirationTimestamp)) {
            return false;
        }

        if (!Objects.equals(this.value, other.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 11 * hash + super.hashCode();
        hash = 11 * hash + Objects.hashCode(this.policyId);
        hash = 11 * hash + Objects.hashCode(this.userId);
        hash = 11 * hash + Objects.hashCode(this.username);
        hash = 11 * hash + Objects.hashCode(this.infractionTimestamp);
        hash = 11 * hash + Objects.hashCode(this.expirationTimestamp);
        hash = 11 * hash + Objects.hashCode(this.value);

        return hash;
    }

    @Override
    public String toString() {
        return "Infraction{" + "id=" + id + ", createdById=" + createdById + ", createdDate=" + createdDate + ", modifiedById=" + modifiedById
               + ", modifiedDate=" + modifiedDate + "policyId=" + policyId + ", userId=" + userId + ", username=" + username
               + ", infractionTimestamp=" + infractionTimestamp + ", expirationTimestamp=" + expirationTimestamp + ", value=" + value + '}';
    }

    /**
     * Returns the timestamp for when the suspension expires.
     *
     * @return The expiration timestamp.
     */
    public Long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    /**
     * Sets the timestamp for when the suspension expires.
     *
     * @param expirationTimestamp The expiration timestamp.
     */
    public void setExpirationTimestamp(Long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    /**
     * Returns the timestamp for when the suspension began.
     *
     * @return The infraction timestamp.
     */
    public Long getInfractionTimestamp() {
        return infractionTimestamp;
    }

    /**
     * Sets the timestamp for when the suspension began.
     *
     * @param infractionTimestamp The infraction timestamp.
     */
    public void setInfractionTimestamp(Long infractionTimestamp) {
        this.infractionTimestamp = infractionTimestamp;
    }

    /**
     * Returns the ID of the policy that was violated.
     *
     * @return The policy ID.
     */
    public BigInteger getPolicyId() {
        return policyId;
    }

    /**
     * Sets the policy ID for the policy that was violated.
     *
     * @param policyId The policy ID.
     */
    public void setPolicyId(BigInteger policyId) {
        this.policyId = policyId;
    }

    /**
     * Returns the ID of the user that violated policy.
     *
     * @return The user ID.
     */
    public BigInteger getUserId() {
        return userId;
    }

    /**
     * Sets the user ID for the user that violated policy.
     *
     * @param userId The user ID.
     */
    public void setUserId(BigInteger userId) {
        this.userId = userId;
    }

    /**
     * Returns the username of the user that violated policy.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for the user that violated policy.
     *
     * @param username The username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the value of the policy metric at the time of suspension.
     *
     * @return The value of the policy metric.
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value of the policy metric at the time the infraction was incurred.
     *
     * @param value The value.
     */
    public void setValue(Double value) {
        this.value = value;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
