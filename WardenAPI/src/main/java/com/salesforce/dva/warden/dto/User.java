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
 * Principal user.
 *
 * @author Jigna Bhatt (jbhatt@salesforce.com) Failing to add comment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("user")
@JsonPropertyOrder(alphabetic = true)
public class User extends Entity {

    private static final long serialVersionUID = 1L;
    private String userName;
    private String email;
    private Boolean privileged;

    @Override
    public User createExample() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setUserName("exampleuser");
        user.setCreatedById(BigInteger.ONE);
        user.setCreatedDate(new Date(1472847819167L));
        user.setModifiedById(BigInteger.TEN);
        user.setModifiedDate(new Date(1472847819167L));
        user.setId(BigInteger.ONE);

        return user;
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

        final User other = (User) obj;

        if (!super.equals(other)) {
            return false;
        }

        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }

        if (!Objects.equals(this.email, other.email)) {
            return false;
        }

        if (!Objects.equals(this.privileged, other.privileged)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 53 * hash + super.hashCode();
        hash = 53 * hash + Objects.hashCode(this.userName);
        hash = 53 * hash + Objects.hashCode(this.email);
        hash = 53 * hash + Objects.hashCode(this.privileged);

        return hash;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", createdById=" + createdById + ", createdDate=" + createdDate + ", modifiedById=" + modifiedById
               + ", modifiedDate=" + modifiedDate + "userName=" + userName + ", email=" + email + ", privileged=" + privileged + '}';
    }

    /**
     * Returns the email.
     *
     * @return The email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email The email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Boolean getPrivileged() {
        return privileged;
    }

    /**
     * Method description
     *
     *
     * @param privileged
     */
    public void setPrivileged(Boolean privileged) {
        this.privileged = privileged;
    }

    /**
     * Returns the user name.
     *
     * @return The user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     *
     * @param userName The user name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
