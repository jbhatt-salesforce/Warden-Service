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

package com.salesforce.dva.warden.ws.dto;

import java.util.Objects;
import javax.ws.rs.Path;
import com.salesforce.dva.warden.ws.resources.AbstractResource;
import com.salesforce.dva.warden.ws.resources.AbstractResource.Description;

/**
 * Endpoint help DTO.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class EndpointHelp implements Comparable<EndpointHelp> {

    private String _endpoint;
    private String _description;

    @Override
    public int compareTo(EndpointHelp o) {
        return String.CASE_INSENSITIVE_ORDER.compare(_endpoint, o.getEndpoint());
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

        final EndpointHelp other = (EndpointHelp) obj;

        if (!Objects.equals(this._endpoint, other._endpoint)) {
            return false;
        }

        if (!Objects.equals(this._description, other._description)) {
            return false;
        }

        return true;
    }

    /**
     * Creates an endpoint help DTO from a resource class.
     *
     * @param   resourceClass  The resource class.
     *
     * @return  The _endpoint help DTO.
     */
    public static EndpointHelp fromResourceClass(Class<? extends AbstractResource> resourceClass) {
        Path path = resourceClass.getAnnotation(Path.class);
        Description description = resourceClass.getAnnotation(Description.class);

        if ((path != null) && (description != null)) {
            EndpointHelp result = new EndpointHelp();

            result.setDescription(description.value());
            result.setEndpoint(path.value());

            return result;
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 47 * hash + Objects.hashCode(this._endpoint);
        hash = 47 * hash + Objects.hashCode(this._description);

        return hash;
    }

    /**
     * Returns the description.
     *
     * @return  The description.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Specifies the description.
     *
     * @param  description  The description.
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Returns the endpoint.
     *
     * @return  The endpoint.
     */
    public String getEndpoint() {
        return _endpoint;
    }

    /**
     * Specifies the endpoint.
     *
     * @param  endpoint  The endpoint.
     */
    public void setEndpoint(String endpoint) {
        _endpoint = endpoint;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



