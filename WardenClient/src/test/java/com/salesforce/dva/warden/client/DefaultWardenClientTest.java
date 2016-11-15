/* Copyright (c) 2014, Salesforce.com, Inc.
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

import com.salesforce.dva.warden.SuspendedException;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import org.junit.Test;
import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class DefaultWardenClientTest extends AbstractTest {

    @Test
    public void testUpdateMetric() throws IOException, SuspendedException {
        try(WardenService wardenService = new WardenService(getMockedClient("/AuthServiceTest.testLoginLogout.json"))) {
            DefaultWardenClient client = new DefaultWardenClient(wardenService, "aUsername", "aPassword");
            Policy policy = new Policy();

            policy.setId(BigInteger.ONE);
            policy.setDefaultValue(0.0);

            String user = "hpotter";

            client.updateMetric(policy, user, 10);
            assertEquals(10.0, client._values.get(client.createKey(policy.getId(), user)), 0.0);
        }
    }

    @Test
    public void testModifyMetric() throws IOException, SuspendedException {
        try(WardenService wardenService = new WardenService(getMockedClient("/AuthServiceTest.testLoginLogout.json"))) {
            DefaultWardenClient client = new DefaultWardenClient(wardenService, "aUsername", "aPassword");
            Policy policy = new Policy();

            policy.setId(BigInteger.ONE);
            policy.setDefaultValue(0.0);

            String user = "hpotter";

            client.modifyMetric(policy, user, 10);
            assertEquals(10.0, client._values.get(client.createKey(policy.getId(), user)), 0.0);
        }
    }

    @Test(expected = SuspendedException.class)
    public void testUpdateMetricSuspendedUser() throws IOException, SuspendedException {
        try(WardenService wardenService = new WardenService(getMockedClient("/AuthServiceTest.testLoginLogout.json"))) {
            Infraction infraction = new Infraction();

            infraction.setPolicyId(BigInteger.ONE);
            infraction.setExpirationTimestamp((long) System.currentTimeMillis() + 600000);

            DefaultWardenClient client = new DefaultWardenClient(wardenService, "aUsername", "aPassword");
            Policy policy = new Policy();

            policy.setId(BigInteger.ONE);
            client._infractions.put(client.createKey(policy.getId(), "hpotter"), infraction);
            client.updateMetric(policy, "hpotter", 10);
        }
    }

    @Test(expected = SuspendedException.class)
    public void testModifyMetricSuspendedUser() throws IOException, SuspendedException {
        try(WardenService wardenService = new WardenService(getMockedClient("/AuthServiceTest.testLoginLogout.json"))) {
            Infraction infraction = new Infraction();

            infraction.setPolicyId(BigInteger.ONE);
            infraction.setExpirationTimestamp((long) System.currentTimeMillis() + 600000);

            DefaultWardenClient client = new DefaultWardenClient(wardenService, "aUsername", "aPassword");
            Policy policy = new Policy();

            policy.setId(BigInteger.ONE);
            client._infractions.put(client.createKey(policy.getId(), "hpotter"), infraction);
            client.modifyMetric(policy, "hpotter", 10);
        }
    }
}
/* Copyright (c) 2014, Salesforce.com, Inc.  All rights reserved. */
