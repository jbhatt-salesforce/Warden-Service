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
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

/**
 * Suspension level for a policy.
 *
 * @author Jigna Bhatt (jbhatt@salesforce.com)
 * @author Tom Valine (tvaline@salesforce.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("suspensionlevel")
@JsonPropertyOrder(alphabetic = true)
public class SuspensionLevel extends Entity {

    //~ Static fields/initializers *******************************************************************************************************************
    private static final long serialVersionUID = 1L;

    //~ Instance fields ******************************************************************************************************************************
    private BigInteger policyId;
    private Integer levelNumber;
    private Integer infractionCount;
    private BigInteger suspensionTime;

    //~ Methods **************************************************************************************************************************************
    @Override
    public SuspensionLevel createExample() {
        SuspensionLevel result = new SuspensionLevel();

        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date(1451606400000L));
        result.setModifiedById(BigInteger.ONE);
        result.setModifiedDate(new Date(1451606400000L));
        result.setPolicyId(BigInteger.ONE);
        result.setLevelNumber(1);
        result.setInfractionCount(4);
        result.setSuspensionTime(BigInteger.TEN);
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

        final SuspensionLevel other = (SuspensionLevel) obj;

        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.policyId, other.policyId)) {
            return false;
        }
        if (!Objects.equals(this.levelNumber, other.levelNumber)) {
            return false;
        }
        if (!Objects.equals(this.infractionCount, other.infractionCount)) {
            return false;
        }
        if (!Objects.equals(this.suspensionTime, other.suspensionTime)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the number of infractions required before a suspension is incurred.
     *
     * @return The infraction count.
     */
    public Integer getInfractionCount() {
        return infractionCount;
    }

    /**
     * Returns the ordinal of the level used to order the levels for a policy.
     *
     * @return The level number.
     */
    public Integer getLevelNumber() {
        return levelNumber;
    }

    /**
     * Returns the policy ID with which the level is associated.
     *
     * @return The policy ID.
     */
    public BigInteger getPolicyId() {
        return policyId;
    }

    /**
     * Returns the duration of a suspension associated with this level.
     *
     * @return The suspension duration.
     */
    public BigInteger getSuspensionTime() {
        return suspensionTime;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 37 * hash + super.hashCode();
        hash = 37 * hash + Objects.hashCode(this.policyId);
        hash = 37 * hash + Objects.hashCode(this.levelNumber);
        hash = 37 * hash + Objects.hashCode(this.infractionCount);
        hash = 37 * hash + Objects.hashCode(this.suspensionTime);
        return hash;
    }

    /**
     * Sets the number of infractions required before a suspension is incurred.
     *
     * @param infractionCount The infraction count.
     */
    public void setInfractionCount(Integer infractionCount) {
        this.infractionCount = infractionCount;
    }

    /**
     * Sets the level ordinal used to order levels associated with a policy.
     *
     * @param levelNumber The level number.
     */
    public void setLevelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
    }

    /**
     * Sets the policy ID with which the level is associated.
     *
     * @param policyId The policy ID.
     */
    public void setPolicyId(BigInteger policyId) {
        this.policyId = policyId;
    }

    /**
     * Sets the suspension duration associated with the level.
     *
     * @param suspensionTime The suspension duration.
     */
    public void setSuspensionTime(BigInteger suspensionTime) {
        this.suspensionTime = suspensionTime;
    }

    @Override
    public String toString() {
        return "SuspensionLevel{" + "id=" + id + ", createdById=" + createdById + ", createdDate=" + createdDate + ", modifiedById=" + modifiedById
                + ", modifiedDate=" + modifiedDate + "policyId=" + policyId + ", levelNumber=" + levelNumber + ", infractionCount=" + infractionCount
                + ", suspensionTime=" + suspensionTime + '}';
    }

}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
