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

import java.util.EnumMap;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Encapsulates web service resource and associated meta-data.
 *
 * @author Tom Valine (tvaline@salesforce.com)
 *
 * @param <T>
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource<T> extends Base {

    private static final long serialVersionUID = 1L;
    private T entity;
    private EnumMap<MetaKey, String> meta;

    /**
     * Meta-data keys.
     *
     * @author Tom Valine (tvaline@salesforce.com)
     */
    public enum MetaKey {

        /**
         * The HREF link for the resource.
         */
        href,

        /**
         * The status of the associated operation.
         */
        status,

        /**
         * The requested operation.
         */
        verb,

        /**
         * The informational message for consumption by API users.
         */
        message,

        /**
         * The informational message for consumption by UI users.
         */
        uiMessage,

        /**
         * The informational message for consumption by Developers users.
         */
        devMessage
    }

    @Override
    public Resource createExample() {
        Resource result = new Resource();
        EnumMap<MetaKey, String> metamap = new EnumMap<>(MetaKey.class);

        metamap.put(MetaKey.href, "http://localhost:8080");
        result.setEntity(new User().createExample());
        result.setMeta(metamap);

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

        final Resource<?> other = (Resource<?>) obj;

        if (!Objects.equals(this.entity, other.entity)) {
            return false;
        }

        if (!Objects.equals(this.meta, other.meta)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 89 * hash + Objects.hashCode(this.entity);
        hash = 89 * hash + Objects.hashCode(this.meta);

        return hash;
    }

    @Override
    public String toString() {
        return "Resource{" + "entity=" + entity + ", meta=" + meta + '}';
    }

    /**
     * Returns the entity associated with the resource.
     *
     * @return The associated entity.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
    )
    @JsonSubTypes( {

        @JsonSubTypes.Type(Policy.class) , @JsonSubTypes.Type(Infraction.class) , @JsonSubTypes.Type(SuspensionLevel.class) ,
        @JsonSubTypes.Type(User.class)

    })
    public T getEntity() {
        return entity;
    }

    /**
     * Sets the entity associated with the resource.
     *
     * @param entity The associated entity.
     */
    public void setEntity(T entity) {
        this.entity = entity;
    }

    /**
     * Returns the meta-data for the resource.
     *
     * @return The meta-data.
     */
    public EnumMap<MetaKey, String> getMeta() {
        return meta;
    }

    /**
     * Sets the meta-data for the resource.
     *
     * @param meta The meta-data.
     */
    public void setMeta(EnumMap<MetaKey, String> meta) {
        this.meta = meta;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
