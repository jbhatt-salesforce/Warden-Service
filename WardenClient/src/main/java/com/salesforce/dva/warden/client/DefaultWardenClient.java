/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.SuspendedException;
import com.salesforce.dva.warden.WardenClient;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Subscription;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import com.salesforce.dva.warden.dto.WardenResource;
import java.net.InetAddress;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DOCUMENT ME!
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
class DefaultWardenClient implements WardenClient {

    final Map<String, Infraction> _infractions;
    final Map<String, Double> _values;
    final WardenService _service;
    final String _username;
    final String _password;
    Thread _updater;
    private String _hostname;
    private Subscription _subscription;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWardenClient.class);

    //~ Constructors *********************************************************************************************************************************
    // This is how the client talks to the server.
    DefaultWardenClient(String endpoint, String username, String password) throws IOException{
        this(WardenService.getInstance(endpoint, 10), username, password);
    }

    /** Creates a new DefaultWardenClient object. */
    DefaultWardenClient(WardenService service, String username, String password) {
         _service = service;
        _infractions = Collections.synchronizedMap(new LinkedHashMap<String, Infraction>(){
            @Override
            protected boolean removeEldestEntry(Map.Entry <String, Infraction> eldest) {
                Long expirationTimestamp = eldest.getValue().getExpirationTimestamp();
                return expirationTimestamp > 0 && expirationTimestamp < System.currentTimeMillis();
            }
        });
        _username = username;
        _password = password;
        _hostname = _getHostname();
        _values = Collections.synchronizedMap(new HashMap<String, Double>());
    }

    private void _initializeUpdaterThread(WardenService service) {
        _updater = new Thread(new MetricUpdater(_values, service));        
        _updater.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            private int limit = 5;
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.warn("Uncaught exception in metric updater thread.  After {} more exceptions, the updater thread will be restarted.", --limit);
                LOGGER.warn(e.getLocalizedMessage(), e);
                if(limit == 0) {
                    LOGGER.warn("Restarting updater thread.");
                    _terminateUpdaterThread();
                    _initializeUpdaterThread(_service);
                }
            }
        });
        _updater.setDaemon(true);
        _updater.start();        
    }

    //~ Methods **************************************************************************************************************************************

    @Override
    public void register(List<Policy> policies, int port) throws IOException {
        AuthService authService = _service.getAuthService();
        authService.login(_username, _password);
        _subscription = _subscribeToEvents(port);
        _initializeUpdaterThread(_service);
        _reconcilePolicies(policies);
    }

    private String _getHostname() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return System.getenv("COMPUTERNAME");
        } else {
            String hostname = System.getenv("HOSTNAME");

            if (hostname != null) {
                return hostname;
            }
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "unknown-host";
        }
    }
    
    private Subscription _subscribeToEvents(int port) throws IOException {
        // @todo start listener thread
        Subscription subscription = new Subscription();
        subscription.setHostname(_hostname);
        subscription.setPort(port);
        subscription = _service.getSubscriptionService().subscribe(subscription).getResources().get(0).getEntity();
        return subscription;
    }

    private List<Policy> _reconcilePolicies(List<Policy> policies) throws IOException {
        PolicyService policyService = _service.getPolicyService();
        List<Policy> result = new ArrayList<>(policies.size());

        //for each policy in this list, obtain it from the server
        for (Policy clientPolicy : policies){
            Policy serverPolicy;
            WardenResponse<Policy> response = policyService.getPolicy(clientPolicy.getService(), clientPolicy.getName());
            if(response.getResources().isEmpty()) {
                serverPolicy = policyService.createPolicies(Arrays.asList(new Policy[]{clientPolicy})).getResources().get(0).getEntity();
            } else {
                serverPolicy = response.getResources().get(0).getEntity();
                if (!clientPolicy.equals(serverPolicy)){
                    clientPolicy.setId(serverPolicy.getId());
                    serverPolicy = policyService.updatePolicy(serverPolicy.getId(), clientPolicy).getResources().get(0).getEntity();
                }
            }

            result.add(serverPolicy);

            List<WardenResource<Infraction>> suspensionResponses = policyService.getSuspensions(serverPolicy.getId()).getResources();

            for (WardenResource<Infraction> wardenResponse : suspensionResponses){
                Infraction suspension = wardenResponse.getEntity();
                _infractions.put(_createKey(suspension.getPolicyId(),suspension.getUserName()), suspension);

            }
        }
        return result;
    }

    @Override
    public void unregister() throws IOException {
        _unsubscribeFromEvents();
        _terminateUpdaterThread();
        // @todo stop listening thread
        _service.getAuthService().logout();
    }

    private void _terminateUpdaterThread() {
        _updater.interrupt();
        try {
            _updater.join(10000);
        } catch (InterruptedException ex) {
            LOGGER.warn("Updater thread failed to stop.  Giving up.");
        }
    }

    private void _unsubscribeFromEvents() throws IOException {
        _service.getSubscriptionService().unsubscribe(_subscription);
    }

    @Override
    public void updateMetric(Policy policy, String user, double value) throws SuspendedException {
        _checkIsSuspended(policy, user);
        _updateLocalValue(policy, user, value, true);
    }

    @Override
    public void modifyMetric(Policy policy, String user, double delta) throws SuspendedException {
        _checkIsSuspended(policy, user);
        _updateLocalValue(policy, user, delta, false);
    }

    private void _checkIsSuspended(Policy policy, String user ) throws SuspendedException {
       Infraction infraction = _infractions.get(_createKey(policy.getId(), user));
        if (infraction != null && (infraction.getExpirationTimestamp()>=System.currentTimeMillis() || infraction.getExpirationTimestamp() <0 )){
            throw new SuspendedException(policy, user, infraction.getExpirationTimestamp(), infraction.getValue());
        }


    }

     String _createKey(BigInteger policyId, String user) {
        return policyId.toString() + ":" + user;
    }

    private void _updateLocalValue(Policy policy, String user, Double value, Boolean replace){
        String key = _createKey(policy.getId(), user);
        Double cachedValue = _values.get(key);

        if (cachedValue == null){
            cachedValue = replace ? value: policy.getDefaultValue()+value;
        } else {
          cachedValue = replace ? value : cachedValue + value;
        }

        _values.put(key, cachedValue);

    }
    
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
