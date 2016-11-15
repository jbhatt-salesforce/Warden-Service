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
package com.salesforce.dva.warden.client;

import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by jbhatt on 10/12/16.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
class MetricUpdater extends Thread {

    //~ Instance fields ******************************************************************************************************************************

    private Map<String, Double> _values;
    private WardenService _wardenService;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new MetricUpdater object.
     *
     * @param  values         DOCUMENT ME!
     * @param  wardenService  DOCUMENT ME!
     */
    MetricUpdater(Map<String, Double> values, WardenService wardenService) {
        this._values = values;
        this._wardenService = wardenService;
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * re-evaluate this to do a batch metric update for multiple user-ids and policy ids. psudo code: put this whole thing in a while (!inturrupted)
     * loop get the current timestamp and truncate it to a whole min copy the results from values to a new Map (temporary) to hold a shorter lock on
     * the values map do a bulk update to the server For testing: write the values to the client and read it back from the server to verify this
     * class. Mock it for now.
     */
    @Override
    public void run() {
        long delta = 0;

        while (!Thread.interrupted()) {
            try {
                Thread.sleep(60000 - delta);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                continue;
            }

            Long start = System.currentTimeMillis();
            Long time = (start / 60000) * 60000;
            Map<String, Double> copyOfValues = new HashMap<>(_values);
            PolicyService policyService = _wardenService.getPolicyService();

            copyOfValues.forEach((String k, Double v) -> {
                List<String> items = Arrays.asList(k.split(":"));
                Map<Long, Double> metric = new HashMap<>();

                metric.put(time, v);
                try {
                    policyService.updateMetricsForUserAndPolicy(new BigInteger(items.get(0)), items.get(1), metric);
                } catch (IOException ex) {
                    LoggerFactory.getLogger(getClass()).warn("Failed to update metric.", ex);
                }
            });
            delta = System.currentTimeMillis() - start;
        }
        _values = null;
        _wardenService = null;
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
