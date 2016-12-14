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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.salesforce.dva.warden.SuspendedException;
import com.salesforce.dva.warden.client.WardenHttpClient.RequestType;
import com.salesforce.dva.warden.dto.Policy;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class MetricUpdaterTest extends AbstractTest {

    private final List<HttpRequestResponse> interceptedRequests = Collections.synchronizedList(new ArrayList<>());

    @Before
    public void beforeTest() {
        interceptedRequests.clear();
    }

    @Test
    public void testPeriodicServerPush() throws IOException, InterruptedException, SuspendedException, Exception {
        try(WardenService wardenService = new WardenService(getMockedClient("/MetricUpdaterTest.testPeriodicServerPush.json"))) {
            DefaultWardenClient client = new DefaultWardenClient(wardenService, "hpotter", "aZkaban");
            Policy policy = _constructUnPersistedPolicy();

            client.register(Arrays.asList(new Policy[] { policy }), 8080);
            policy.setId(BigInteger.ONE);
            client.updateMetric(policy, "hpotter", 1.0);
            Thread.currentThread().sleep(90000);
            client.unregister();
            assertTrue(interceptedRequests.size() == 1);
            assertTrue(interceptedRequests.get(0).getJsonInput().contains("1.0"));
            assertTrue(interceptedRequests.get(0).getEndpoint().equals("/policy/" + policy.getId() + "/user/hpotter/metric"));
            assertTrue(interceptedRequests.get(0).getType().equals(RequestType.PUT));
        }
    }

    @Override
    protected void processRequest(HttpRequestResponse step) {
        interceptedRequests.add(step);
    }

    private Policy _constructUnPersistedPolicy() throws JsonProcessingException {
        Policy result = new Policy();

        result.setService("TestService");
        result.setName("TestName");
        result.setOwners(Arrays.asList("TestOwner"));
        result.setUsers(Arrays.asList("TestUser"));
        result.setSubSystem("TestSubSystem");
        result.setTriggerType(Policy.TriggerType.BETWEEN);
        result.setAggregator(Policy.Aggregator.AVG);
        result.setThresholds(Arrays.asList(0.0));
        result.setTimeUnit("5min");
        result.setDefaultValue(0.0);
        result.setCronEntry("0 */4 * * *");
        return result;
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
