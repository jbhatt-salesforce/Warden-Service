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
import com.salesforce.dva.warden.dto.*;
import com.salesforce.dva.warden.dto.Resource.MetaKey;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 2017
 * @author         Tom Valine (tvaline@salesforce.com)
 */
public class UserServiceTest extends AbstractTest {

    private Infraction _constructPersistedInfraction() throws JsonProcessingException {
        Infraction result = new Infraction();

        result.setPolicyId(BigInteger.ONE);
        result.setUserId(BigInteger.ONE);
        result.setUsername("hpotter");
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

        return result;
    }

    private WardenResponse<User> _constructPersistedResponse(String httpVerb) throws JsonProcessingException {
        User persistedUser = _constructPersistedUser();
        WardenResponse<User> result = new WardenResponse<>();
        Resource<User> resource = new Resource<>();
        List<Resource<User>> resources = new ArrayList<>(1);
        EnumMap<MetaKey, String> meta = new EnumMap<>(MetaKey.class);

        meta.put(MetaKey.href, "TestHref");
        meta.put(MetaKey.devMessage, "TestDevMessage");
        meta.put(MetaKey.message, "TestMessage");
        meta.put(MetaKey.status, "200");
        meta.put(MetaKey.uiMessage, "TestUIMessage");
        meta.put(MetaKey.verb, httpVerb);
        persistedUser.setId(BigInteger.ONE);
        resource.setEntity(persistedUser);
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

    private WardenResponse<Policy> _constructPersistedResponsePolicy(String httpVerb) throws JsonProcessingException {
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

    private User _constructPersistedUser() throws JsonProcessingException {
        User result = new User();

        result.setEmail("user@user.com");
        result.setUsername("exampleuser");
        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date(1472847819167L));
        result.setModifiedById(BigInteger.TEN);
        result.setModifiedDate(new Date(1472847819167L));
        result.setId(BigInteger.ONE);

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

        return result;
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Test
    public void testGetInfractionsForUser() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/UserServiceTests.json"))) {
            UserService userService = wardenService.getUserService();
            WardenResponse<Infraction> expectedResponse = _constructPersistedResponseInfraction("GET");
            WardenResponse<Infraction> actualResponse = userService.getInfractionsForUser("hpotter");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Test
    public void testGetInfractionsForUserAndPolicy() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/UserServiceTests.json"))) {
            UserService userService = wardenService.getUserService();
            WardenResponse<Infraction> expectedResponse = _constructPersistedResponseInfraction("GET");
            WardenResponse<Infraction> actualResponse = userService.getInfractionsForUserAndPolicy("hpotter", BigInteger.ONE);

            assertEquals(expectedResponse, actualResponse);
        }
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Test
    public void testGetPoliciesByUser() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/UserServiceTests.json"))) {
            UserService userService = wardenService.getUserService();
            WardenResponse<Policy> expectedResponse = _constructPersistedResponsePolicy("GET");
            WardenResponse<Policy> actualResponse = userService.getPoliciesForUser("hpotter");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Test
    public void testGetSuspensionsionForUser() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/UserServiceTests.json"))) {
            UserService userService = wardenService.getUserService();
            WardenResponse<Infraction> expectedResponse = _constructPersistedResponseInfraction("GET");
            WardenResponse<Infraction> actualResponse = userService.getSuspensionForUser("hpotter", BigInteger.ONE);

            assertEquals(expectedResponse, actualResponse);
        }
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Test
    public void testGetSuspensionsionsForUser() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/UserServiceTests.json"))) {
            UserService userService = wardenService.getUserService();
            WardenResponse<Infraction> expectedResponse = _constructPersistedResponseInfraction("GET");
            WardenResponse<Infraction> actualResponse = userService.getSuspensionsForUser("hpotter");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Test
    public void testGetUserById() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/UserServiceTests.json"))) {
            UserService userService = wardenService.getUserService();
            WardenResponse<User> expectedResponse = _constructPersistedResponse("GET");
            WardenResponse<User> actualResponse = userService.getUserByUsername("hpotter");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Test
    public void testGetUsers() throws IOException {
        try (WardenService wardenService = new WardenService(getMockedClient("/UserServiceTests.json"))) {
            UserService userService = wardenService.getUserService();
            WardenResponse<User> expectedResponse = _constructPersistedResponse("GET");
            WardenResponse<User> actualResponse = userService.getUsers();

            assertEquals(expectedResponse, actualResponse);
        }
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



