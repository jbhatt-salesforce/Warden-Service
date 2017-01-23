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

package com.salesforce.dva.warden;

import java.util.List;
import com.salesforce.dva.warden.dto.Policy;

/**
 * Embedded Warden client.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
public interface WardenClient {

    /**
     * Updates the existing value of a policy metric. This method is intended to be used as a pre-condition check for protected operations.
     *
     * @param   policy    The associated policy.
     * @param   username  The username for which to update the usage metric.
     * @param   delta     The amount of change to apply.
     *
     * @throws  SuspendedException  If the user is suspended for the specified policy.
     */
    void modifyMetric(Policy policy, String username, double delta) throws SuspendedException;

    /**
     * This method is responsible for establishing communication with the warden server. Implementations of the client must reconcile the state of the
     * provided policies with the server, subscribe to infraction events from the server and periodically publish usage data for policies to the
     * server.
     *
     * @param   policies  The policies to reconcile.
     * @param   port      The port on which to listen for infraction events from the server.
     *
     * @throws  Exception  If an error occurs while registering.
     */
    void register(List<Policy> policies, int port) throws Exception;

    /**
     * Terminates communication with the server. Implementations of the client must unsubscribe from events, terminate the publication of usage data
     * and flush any unpublished usage data to the server.
     *
     * @throws  Exception  If an error occurs while unregistering.
     */
    void unregister() throws Exception;

    /**
     * Replaces the existing value of a policy metric. This method is intended to be used as a pre-condition check for protected operations.
     *
     * @param   policy    The associated policy.
     * @param   username  The username for which to update the usage metric.
     * @param   value     The amount of change to apply.
     *
     * @throws  SuspendedException  If the user is suspended for the specified policy.
     */
    void updateMetric(Policy policy, String username, double value) throws SuspendedException;

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
