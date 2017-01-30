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

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Authentication credentials.
 *
 * @author Tom Valine (tvaline@salesforce.com)
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Credentials extends Base {

    private static final long serialVersionUID = 1L;
    private String userName;
    private String password;

    @Override
    public Credentials createExample() {
        Credentials result = new Credentials();

        result.setPassword("aPassword");
        result.setUserName("aUserName");

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

        final Credentials other = (Credentials) obj;

        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }

        if (!Objects.equals(this.password, other.password)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 13 * hash + Objects.hashCode(this.userName);
        hash = 13 * hash + Objects.hashCode(this.password);

        return hash;
    }

    @Override
    public String toString() {
        return "Credentials{" + "userName=" + userName + ", password=********}";
    }

    /**
     * Returns the password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the userName.
     *
     * @return The userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the userName.
     *
     * @param userName The userName.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
