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

import java.io.IOException;
import com.salesforce.dva.warden.client.WardenService.EndpointService;
import com.salesforce.dva.warden.dto.Subscription;
import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;

/**
 * Provides methods to subscribe and unsubscribe from server events.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
final class SubscriptionService extends EndpointService {

    private static final String REQUESTURL = "/subscription";

    /**
     * Creates a new SubscriptionService object.
     *
     * @param  client  The HTTP client to use. Cannot be null.
     */
    SubscriptionService(WardenHttpClient client) {
        super(client);
    }

    /**
     * Subscribes to events from the server.
     *
     * @param   subscription  The subscription information. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Subscription> subscribe(Subscription subscription) throws IOException {
        requireThat(subscription != null, "The subscription cannot be null.");

        String requestUrl = REQUESTURL;

        return getClient().executeHttpRequest(WardenHttpClient.RequestType.POST, requestUrl, subscription);
    }

    /**
     * Unsubscribe from receiving server events.
     *
     * @param   subscription  The subscription information. Cannot be null.
     *
     * @return  The response object containing relevant details about the operation.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    WardenResponse<Subscription> unsubscribe(Subscription subscription) throws IOException {
        requireThat(subscription != null, "The subscription cannot be null.");

        String requestUrl = REQUESTURL + "/" + subscription.getId().toString();

        return getClient().executeHttpRequest(WardenHttpClient.RequestType.DELETE, requestUrl, null);
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
