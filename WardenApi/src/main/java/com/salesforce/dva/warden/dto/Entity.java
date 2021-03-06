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

package com.salesforce.dva.warden.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * The base entity DTO.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
@SuppressWarnings("serial")
public abstract class Entity extends Base implements Serializable {

    //~ Instance fields ******************************************************************************************************************************

    private BigInteger id;
    private BigInteger createdById;
    private Date createdDate;
    private BigInteger modifiedById;
    private Date modifiedDate;


    //~ Methods **************************************************************************************************************************************

    /**
     * Returns the entity ID.
     *
     * @return  The entity ID.
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Specifies the entity ID.
     *
     * @param  id  The entity ID.
     */
    public void setId(BigInteger id) {
        this.id = id;
    }

    /**
     * Returns the created date.
     *
     * @return  The created date.
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Specifies the created date.
     *
     * @param  createdDate  The created date.
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Returns the ID of the creator.
     *
     * @return  The ID of the creator.
     */
    public BigInteger getCreatedById() {
        return createdById;
    }

    /**
     * Specifies the ID of the creator.
     *
     * @param  createdById  The ID of the creator.
     */
    public void setCreatedById(BigInteger createdById) {
        this.createdById = createdById;
    }

    /**
     * Returns the ID of the last person who modified the entity.
     *
     * @return  The ID of the last person who modified the entity.
     */
    public BigInteger getModifiedById() {
        return modifiedById;
    }

    /**
     * Specifies the ID of the person who most recently modified the entity.
     *
     * @param  modifiedById  The ID of the person who most recently modified the entity.
     */
    public void setModifiedById(BigInteger modifiedById) {
        this.modifiedById = modifiedById;
    }

    /**
     * Returns the modified on date.
     *
     * @return  The modified on date.
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * Specifies the modified on date.
     *
     * @param  modifiedDate  The modified on date.
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
