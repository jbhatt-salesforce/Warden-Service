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
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.dto.Resource.MetaKey;
import com.salesforce.dva.warden.dto.SuspensionLevel;

public class PolicyServiceTest extends AbstractTest {

    private Infraction _constructPersistedInfraction() throws JsonProcessingException {
        Infraction result = new Infraction();

        result.setPolicyId(BigInteger.ONE);
        result.setUserId(BigInteger.ONE);
        result.setUserName("hpotter");
        result.setInfractionTimestamp(100000L);
        result.setExpirationTimestamp(-1L);
        result.setValue(Double.valueOf(10.0));
        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date(1472847819167L));
        result.setModifiedById(BigInteger.TEN);
        result.setModifiedDate(new Date(1472847819167L));

        return result;
    }

    private Policy _constructPersistedPolicy() throws JsonProcessingException {
        Policy result = _constructUnPersistedPolicy();

        result.setId(BigInteger.ONE);
        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date(1472847819167L));
        result.setModifiedById(BigInteger.TEN);
        result.setModifiedDate(new Date(1472847819167L));
        result.getSuspensionLevels().get(0).setId(BigInteger.ONE);

        return result;
    }

    private WardenResponse<Policy> _constructPersistedResponse(String httpVerb) throws JsonProcessingException {
        Policy persistedPolicy = _constructPersistedPolicy();
        WardenResponse<Policy> result = new WardenResponse<>();
        Resource<Policy> resource = new Resource<>();
        List<Resource<Policy>> resources = new ArrayList<>(1);
        EnumMap<MetaKey, String> meta = new EnumMap<>(MetaKey.class);

        meta.put(MetaKey.href, "TestHref");
        meta.put(MetaKey.devMessage, "TestDevMessage");
        meta.put(MetaKey.message, "TestMessage");
        meta.put(MetaKey.status, "200");
        meta.put(MetaKey.uiMessage, "TestUIMessage");
        meta.put(MetaKey.verb, httpVerb);
        persistedPolicy.setId(BigInteger.ONE);
        resource.setEntity(persistedPolicy);
        resource.setMeta(meta);
        resources.add(resource);
        result.setMessage("success");
        result.setStatus(200);
        result.setResources(resources);

        return result;
    }

    private WardenResponse<Infraction> _constructPersistedResponseInfraction(String httpVerb) throws JsonProcessingException {
        WardenResponse<Infraction> result = new WardenResponse<>();
        EnumMap<MetaKey, String> meta = new EnumMap<>(MetaKey.class);

        meta.put(MetaKey.href, "TestHref");
        meta.put(MetaKey.devMessage, "TestDevMessage");
        meta.put(MetaKey.message, "TestMessage");
        meta.put(MetaKey.status, "200");
        meta.put(MetaKey.uiMessage, "TestUIMessage");
        meta.put(MetaKey.verb, httpVerb);

        Resource<Infraction> resource = new Resource<>();
        Infraction infraction = _constructPersistedInfraction();

        resource.setEntity(infraction);
        resource.setMeta(meta);

        List<Resource<Infraction>> resources = new ArrayList<>(1);

        infraction.setId(BigInteger.ONE);
        resources.add(resource);
        result.setMessage("success");
        result.setStatus(200);
        result.setResources(resources);

        return result;
    }

    private WardenResponse<SuspensionLevel> _constructPersistedResponseSuspensionLevel(String httpVerb) throws JsonProcessingException {
        WardenResponse<SuspensionLevel> result = new WardenResponse<>();
        EnumMap<MetaKey, String> meta = new EnumMap<>(MetaKey.class);

        meta.put(MetaKey.href, "TestHref");
        meta.put(MetaKey.devMessage, "TestDevMessage");
        meta.put(MetaKey.message, "TestMessage");
        meta.put(MetaKey.status, "200");
        meta.put(MetaKey.uiMessage, "TestUIMessage");
        meta.put(MetaKey.verb, httpVerb);

        Resource<SuspensionLevel> resource = new Resource<>();
        SuspensionLevel level = _constructPersistedSuspensionLevel();

        resource.setEntity(level);
        resource.setMeta(meta);

        List<Resource<SuspensionLevel>> resources = new ArrayList<>(1);

        level.setId(BigInteger.ONE);
        resources.add(resource);
        result.setMessage("success");
        result.setStatus(200);
        result.setResources(resources);

        return result;
    }

    private SuspensionLevel _constructPersistedSuspensionLevel() throws JsonProcessingException {
        SuspensionLevel result = _constructUnPersistedSuspensionLevel();

        result.setId(BigInteger.ONE);
        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date(1472847819167L));
        result.setModifiedById(BigInteger.TEN);
        result.setModifiedDate(new Date(1472847819167L));

        return result;
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
        result.setSuspensionLevels(Arrays.asList(new SuspensionLevel[]{_constructPersistedSuspensionLevel()}));

        return result;
    }

    private SuspensionLevel _constructUnPersistedSuspensionLevel() throws JsonProcessingException {
        SuspensionLevel result = new SuspensionLevel();

        result.setPolicyId(BigInteger.ONE);
        result.setLevelNumber(1);
        result.setInfractionCount(1);
        result.setSuspensionTime(BigInteger.valueOf(3600000));

        return result;
    }

    @Test
    public void testCreatePolicies() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            List<Policy> policies = Arrays.asList(new Policy[] { _constructUnPersistedPolicy() });
            WardenResponse<Policy> actualResponse = policyService.createPolicies(policies);
            WardenResponse<Policy> expectedResponse = _constructPersistedResponse("POST");
            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testCreateSuspensionLevels() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            List<SuspensionLevel> suspensionLevels = Arrays.asList(new SuspensionLevel[] { _constructUnPersistedSuspensionLevel() });
            WardenResponse<SuspensionLevel> actualResponse = policyService.createSuspensionLevels(BigInteger.ONE, suspensionLevels);
            WardenResponse<SuspensionLevel> expectedResponse = _constructPersistedResponseSuspensionLevel("POST");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testDeletePolicies() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            Set<BigInteger> policyIds = new HashSet(Arrays.asList(new BigInteger[] { BigInteger.ONE }));
            WardenResponse<Policy> actualResponse = policyService.deletePolicies(policyIds);
            WardenResponse<Policy> expectedResponse = _constructPersistedResponse("DELETE");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testDeletePolicy() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Policy> actualResponse = policyService.deletePolicy(BigInteger.ONE);
            WardenResponse<Policy> expectedResponse = _constructPersistedResponse("DELETE");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testDeleteSuspensionLevel() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<SuspensionLevel> actualResponse = policyService.deleteSuspensionLevel(BigInteger.ONE, BigInteger.ONE);
            WardenResponse<SuspensionLevel> expectedResponse = _constructPersistedResponseSuspensionLevel("DELETE");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testDeleteSuspensionLevels() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<SuspensionLevel> actualResponse = policyService.deleteSuspensionLevels(BigInteger.ONE);
            WardenResponse<SuspensionLevel> expectedResponse = _constructPersistedResponseSuspensionLevel("DELETE");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testDeleteSuspensions() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Infraction> actual = policyService.deleteSuspensions(BigInteger.ONE);
            WardenResponse<Infraction> expected = _constructPersistedResponseInfraction("DELETE");

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testDeleteSuspensionsForUserAndPolicy() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Infraction> actual = policyService.deleteSuspensionsForUserAndPolicy(BigInteger.ONE, "hpotter");
            WardenResponse<Infraction> expected = _constructPersistedResponseInfraction("DELETE");

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetInfraction() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Infraction> actual = policyService.getInfraction(BigInteger.ONE, BigInteger.ONE);
            WardenResponse<Infraction> expected = _constructPersistedResponseInfraction("GET");

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetInfractions() throws IOException {
        _constructPersistedInfraction();

        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Infraction> actual = policyService.getInfractions(BigInteger.ONE);
            WardenResponse<Infraction> expected = _constructPersistedResponseInfraction("GET");

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetPolicies() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Policy> expectedResponse = _constructPersistedResponse("GET");
            WardenResponse<Policy> actualResponse = policyService.getPolicies();
            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testGetPolicy() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Policy> expectedResponse = _constructPersistedResponse("GET");
            WardenResponse<Policy> actualResponse = policyService.getPolicy(BigInteger.ONE);

            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testGetSuspensionLevel() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<SuspensionLevel> actual = policyService.getSuspensionLevel(BigInteger.ONE, BigInteger.ONE);
            WardenResponse<SuspensionLevel> expected = _constructPersistedResponseSuspensionLevel("GET");

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetSuspensionLevels() throws IOException {
        _constructPersistedResponseSuspensionLevel("GET");

        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<SuspensionLevel> actual = policyService.getSuspensionLevels(BigInteger.ONE);
            WardenResponse<SuspensionLevel> expected = _constructPersistedResponseSuspensionLevel("GET");

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetSuspensions() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Infraction> actual = policyService.getSuspensions(BigInteger.ONE);
            WardenResponse<Infraction> expected = _constructPersistedResponseInfraction("GET");

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetSuspensionsForUserAndPolicy() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Infraction> actual = policyService.getSuspensionsForUserAndPolicy(BigInteger.ONE, "hpotter");
            WardenResponse<Infraction> expected = _constructPersistedResponseInfraction("GET");

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testUpdatePolicies() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            List<Policy> policies = Arrays.asList(new Policy[] { _constructPersistedPolicy() });
            WardenResponse<Policy> actualResponse = policyService.updatePolicies(policies);
            WardenResponse<Policy> expectedResponse = _constructPersistedResponse("PUT");
            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testUpdatePolicy() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            WardenResponse<Policy> policyWardenResponse = policyService.getPolicy(BigInteger.ONE);
            Policy policy = policyWardenResponse.getResources().get(0).getEntity();

            policy.setAggregator(Policy.Aggregator.ZIMSUM);
            WardenResponse<Policy> actualResponse = policyService.updatePolicy(BigInteger.ONE, policy);
            WardenResponse<Policy> expectedResponse = _constructPersistedResponse("PUT");

            expectedResponse.getResources().get(0).getEntity().setAggregator(Policy.Aggregator.ZIMSUM);
            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testUpdateSuspensionLevel() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            SuspensionLevel suspensionLevel = _constructPersistedSuspensionLevel();

            suspensionLevel.setInfractionCount(2);

            WardenResponse<SuspensionLevel> actualResponse = policyService.updateSuspensionLevel(BigInteger.ONE, BigInteger.ONE, suspensionLevel);
            WardenResponse<SuspensionLevel> expectedResponse = _constructPersistedResponseSuspensionLevel("PUT");

            expectedResponse.getResources().get(0).getEntity().setInfractionCount(2);
            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    public void testUpdateSuspensionLevels() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/PolicyServiceTest.testGetPolicies.json"))) {
            PolicyService policyService = wardenService.getPolicyService();
            List<SuspensionLevel> suspensionLevels = Arrays.asList(new SuspensionLevel[] { _constructPersistedSuspensionLevel() });

            suspensionLevels.get(0).setInfractionCount(2);

            WardenResponse<SuspensionLevel> actualResponse = policyService.updateSuspensionLevels(BigInteger.ONE, suspensionLevels);
            WardenResponse<SuspensionLevel> expectedResponse = _constructPersistedResponseSuspensionLevel("PUT");

            expectedResponse.getResources().get(0).getEntity().setInfractionCount(2);
            assertEquals(expectedResponse, actualResponse);
        }
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */


