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
 * Server subscription.
 *
 * @author Jigna Bhatt (jbhatt@salesforce.com)
 */
@JsonTypeName("subscription")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subscription extends Entity {

    //~ Static fields/initializers *******************************************************************************************************************
    private static final long serialVersionUID = 1L;

    //~ Instance fields ******************************************************************************************************************************
    private String hostname;
    private Integer port;

    //~ Methods **************************************************************************************************************************************
    @Override
    public Subscription createExample() {
        Subscription result = new Subscription();

        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date(1451606400000L));
        result.setHostname("example.client.com");
        result.setModifiedById(BigInteger.ONE);
        result.setModifiedDate(new Date(1451606400000L));
        result.setPort(8080);
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

        final Subscription other = (Subscription) obj;

        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.hostname, other.hostname)) {
            return false;
        }
        if (!Objects.equals(this.port, other.port)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the host name.
     *
     * @return The host name.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the port.
     *
     * @return The port.
     */
    public Integer getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 97 * hash + super.hashCode();
        hash = 97 * hash + Objects.hashCode(this.hostname);
        hash = 97 * hash + Objects.hashCode(this.port);
        return hash;
    }

    /**
     * Sets the host name.
     *
     * @param hostname The host name.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Sets the port.
     *
     * @param port The port.
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Subscription{" + "id=" + id + ", createdById=" + createdById + ", createdDate=" + createdDate + ", modifiedById=" + modifiedById
                + ", modifiedDate=" + modifiedDate + "hostname=" + hostname + ", port=" + port + '}';
    }

}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
