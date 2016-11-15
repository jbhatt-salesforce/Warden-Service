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
import com.salesforce.dva.warden.WardenClient;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Subscription;
import com.salesforce.dva.warden.dto.WardenResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
class DefaultWardenClient implements WardenClient {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWardenClient.class);

    //~ Instance fields ******************************************************************************************************************************

    final Map<String, Infraction> _infractions;
    final Map<String, Double> _values;
    final WardenService _service;
    final String _username;
    final String _password;
    Thread _updater;
    Thread _listener;
    private String _hostname;
    private Subscription _subscription;

    //~ Constructors *********************************************************************************************************************************

    /**
     * This is how the client talks to the server.
     *
     * @param   endpoint  DOCUMENT ME!
     * @param   username  DOCUMENT ME!
     * @param   password  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    DefaultWardenClient(String endpoint, String username, String password) throws IOException {
        this(WardenService.getInstance(endpoint, 10), username, password);
    }

    /**
     * Creates a new DefaultWardenClient object.
     *
     * @param  service   DOCUMENT ME!
     * @param  username  DOCUMENT ME!
     * @param  password  DOCUMENT ME!
     */
    DefaultWardenClient(WardenService service, String username, String password) {
        _service = service;
        _infractions = Collections.synchronizedMap(new InfractionCache());
        _username = username;
        _password = password;
        _hostname = _getHostname();
        _values = Collections.synchronizedMap(new HashMap<String, Double>());
    }
    
    private static class InfractionCache extends LinkedHashMap<String, Infraction> {
        
        private static final long serialVersionUID = 1L;
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Infraction> eldest) {
            Long expirationTimestamp = eldest.getValue().getExpirationTimestamp();

            return expirationTimestamp > 0 && expirationTimestamp < System.currentTimeMillis();
        }
    }

    //~ Methods **************************************************************************************************************************************

    static String createKey(BigInteger policyId, String user) {
        return policyId.toString() + ":" + user;
    }

    //~ Methods **************************************************************************************************************************************

    @Override
    public void modifyMetric(Policy policy, String user, double delta) throws SuspendedException {
        _checkIsSuspended(policy, user);
        _updateLocalValue(policy, user, delta, false);
    }

    @Override
    public void register(List<Policy> policies, int port) throws IOException {
        AuthService authService = _service.getAuthService();

        authService.login(_username, _password);
        _subscription = _subscribeToEvents(port);
        _initializeUpdaterThread(_service);
        _reconcilePolicies(policies);
    }

    @Override
    public void unregister() throws IOException {
        _unsubscribeFromEvents();
        _terminateUpdaterThread();

        _service.getAuthService().logout();
    }

    @Override
    public void updateMetric(Policy policy, String user, double value) throws SuspendedException {
        _checkIsSuspended(policy, user);
        _updateLocalValue(policy, user, value, true);
    }

    private void _checkIsSuspended(Policy policy, String user) throws SuspendedException {
        Infraction infraction = _infractions.get(createKey(policy.getId(), user));

        if (infraction != null && (infraction.getExpirationTimestamp() >= System.currentTimeMillis() || infraction.getExpirationTimestamp() < 0)) {
            throw new SuspendedException(policy, user, infraction.getExpirationTimestamp(), infraction.getValue());
        }
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

    private void _initializeEventListener(int port) {
        try {
            _listener = new EventListener(_infractions, _service, port);
        } catch (SocketException ex) {
            throw new RuntimeException(ex);
        }
        _listener.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.warn("Uncaught exception in event listener.  Restarting listener thread.", e);
                    _terminateEventListener();
                    _initializeEventListener(port);
                }
            });
        _listener.setDaemon(true);
        _listener.start();
    }

    private void _initializeUpdaterThread(WardenService service) {
        _updater = new MetricUpdater(_values, service);
        _updater.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.warn("Uncaught exception in metric updater thread.  Restarting updater thread.", e);
                    _terminateUpdaterThread();
                    _initializeUpdaterThread(_service);
                }
            });
        _updater.setDaemon(true);
        _updater.start();
    }

    private List<Policy> _reconcilePolicies(List<Policy> policies) throws IOException {
        PolicyService policyService = _service.getPolicyService();
        List<Policy> result = new ArrayList<>(policies.size());

        // for each policy in this list, obtain it from the server
        for (Policy clientPolicy : policies) {
            Policy serverPolicy;
            WardenResponse<Policy> response = policyService.getPolicy(clientPolicy.getService(), clientPolicy.getName());

            if (response.getResources().isEmpty()) {
                serverPolicy = policyService.createPolicies(Arrays.asList(new Policy[] { clientPolicy })).getResources().get(0).getEntity();
            } else {
                serverPolicy = response.getResources().get(0).getEntity();
                if (!clientPolicy.equals(serverPolicy)) {
                    clientPolicy.setId(serverPolicy.getId());
                    serverPolicy = policyService.updatePolicy(serverPolicy.getId(), clientPolicy).getResources().get(0).getEntity();
                }
            }
            result.add(serverPolicy);

            List<WardenResource<Infraction>> suspensionResponses = policyService.getSuspensions(serverPolicy.getId()).getResources();

            for (WardenResource<Infraction> wardenResponse : suspensionResponses) {
                Infraction suspension = wardenResponse.getEntity();

                _infractions.put(createKey(suspension.getPolicyId(), suspension.getUserName()), suspension);
            }
        }
        return result;
    }

    private Subscription _subscribeToEvents(int port) throws IOException {
        _initializeEventListener(port);

        Subscription subscription = new Subscription();

        subscription.setHostname(_hostname);
        subscription.setPort(port);
        subscription = _service.getSubscriptionService().subscribe(subscription).getResources().get(0).getEntity();
        return subscription;
    }

    private void _terminateEventListener() {
        _listener.interrupt();
        try {
            _listener.join(10000);
        } catch (InterruptedException ex) {
            LOGGER.warn("Listener thread failed to stop.  Giving up.");
        }
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
        _terminateEventListener();
    }

    private void _updateLocalValue(Policy policy, String user, Double value, Boolean replace) {
        String key = createKey(policy.getId(), user);
        Double cachedValue = _values.get(key);

        if (cachedValue == null) {
            cachedValue = replace ? value : policy.getDefaultValue() + value;
        } else {
            cachedValue = replace ? value : cachedValue + value;
        }
        _values.put(key, cachedValue);
    }
}
/* Copyright (c) 2014, Salesforce.com, Inc.  All rights reserved. */
