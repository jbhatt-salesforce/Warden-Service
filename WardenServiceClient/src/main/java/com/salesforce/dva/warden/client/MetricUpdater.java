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
import java.math.BigInteger;
import java.util.*;
import org.slf4j.LoggerFactory;
import com.salesforce.dva.warden.client.DefaultWardenClient.ValueCache;

/**
 * Periodically pushes cached policy values for users to the server..
 *
 * @author Jigna Bhatt (jbhatt@salesforce.com)
 * @author Tom Valine (tvaline@salesforce.com)
 */
class MetricUpdater implements Runnable {

    private final ValueCache _values;
    private final WardenService _wardenService;

    /**
     * Creates a new MetricUpdater object.
     *
     * @param values The value cache containing the metric values to push to the server. Cannot be null. Must be thread safe.
     * @param wardenService The warden service to use. Cannot be null.
     */
    MetricUpdater(ValueCache values, WardenService wardenService) {
        _values = values;
        _wardenService = wardenService;
    }

    @Override
    public void run() {

        Long start = System.currentTimeMillis();
        Long time = (start / 60000) * 60000;
        Map<String, Double> copyOfValues = new HashMap<>(_values);
        PolicyService policyService = _wardenService.getPolicyService();

        copyOfValues.forEach(
                (String k, Double v) -> {
                    List<Object> items = _values.getKeyComponents(k);
                    Map<Long, Double> metric = new HashMap<>();

                    metric.put(time, v);

                    try {
                        policyService.updateMetricsForUserAndPolicy(BigInteger.class.cast(items.get(0)), items.get(1).toString(), v);
                    } catch (IOException ex) {
                        LoggerFactory.getLogger(getClass()).warn("Failed to update metric.", ex);
                    }
                });

    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */