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

package com.salesforce.dva.warden.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dva.warden.dto.Resource;
import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;

/**
 * The Warden request response object which encapsulates information about a completed request.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 *
 * @param <T>
 */
public class WardenResponse<T> {

    @JsonInclude(Include.NON_NULL)
    private List<Resource<T>> _resources;
    private int _status;
    private String _message;

    /** Creates a new WardenResponse object. */
    WardenResponse() {}

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

        final WardenResponse<?> other = (WardenResponse<?>) obj;

        if (this._status != other._status) {
            return false;
        }

        if (!Objects.equals(this._message, other._message)) {
            return false;
        }

        if (!Objects.equals(this._resources, other._resources)) {
            return false;
        }

        return true;
    }

    /**
     * Helper method to create a response from the raw HTTP response.
     *
     * @param   <T>       The return entity type parameter.
     * @param   response  The HTTP response. Cannot be null.
     *
     * @return  The wrapped response.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    static <T> WardenResponse<T> generateResponse(HttpResponse response) throws IOException {
        requireThat(response != null, "The response cannot be null.");
        EntityUtils.consume(response.getEntity());

        int status = response.getStatusLine().getStatusCode();
        String message = response.getStatusLine().getReasonPhrase();
        HttpEntity entity = response.getEntity();
        List<Resource<T>> resources = new ArrayList<>();

        if (entity != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                entity.writeTo(baos);

                String payload = baos.toString("UTF-8");

                resources.addAll(new ObjectMapper().readValue(payload, new TypeReference<List<Resource<T>>>() {}
                ));
            }
        }

        WardenResponse result = new WardenResponse();

        result.setMessage(message);
        result.setStatus(status);
        result.setResources(resources);

        return result;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 73 * hash + Objects.hashCode(this._resources);
        hash = 73 * hash + this._status;
        hash = 73 * hash + Objects.hashCode(this._message);

        return hash;
    }

    /**
     * The message associated with the response.
     *
     * @return  The message associated with the response.
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Sets the response message.
     *
     * @param  message  The response message. May be null.
     */
    void setMessage(String message) {
        this._message = message;
    }

    /**
     * Returns the list of resources returned in the response.
     *
     * @return  The list of resources. Will never be null, but may be empty.
     */
    public List<Resource<T>> getResources() {
        return _resources;
    }

    /**
     * Sets the response resources.
     *
     * @param  resources  The response resources. Cannot be null.
     */
    void setResources(List<Resource<T>> resources) {
        requireThat(resources != null, "Resources cannot be null.");

        this._resources = resources;
    }

    /**
     * Returns the HTTP status code for the response.
     *
     * @return  The HTTP status code for the response.
     */
    public int getStatus() {
        return _status;
    }

    /**
     * Sets the HTTP status code.
     *
     * @param  status  The status code.
     */
    void setStatus(int status) {
        this._status = status;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
