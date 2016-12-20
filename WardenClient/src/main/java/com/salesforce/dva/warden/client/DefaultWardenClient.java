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

import com.salesforce.dva.warden.SuspendedException;
import com.salesforce.dva.warden.WardenClient;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.dto.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;

/**
 * Default implementation of the WardenClient interface.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 * @author  Tom Valine (tvaline@salesforce.com)
 */
class DefaultWardenClient implements WardenClient {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWardenClient.class);

    //~ Instance fields ******************************************************************************************************************************

    final InfractionCache infractions;
    final ValueCache values;
    final WardenService service;
    final String username;
    final String password;
    private Thread _updater;
    private EventServer _listener;
    private String _hostname;
    private Subscription _subscription;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new DefaultWardenClient.
     *
     * @param   endpoint  The Warden web service endpoint. Cannot be null or empty.
     * @param   username  The username to authenticate with. Cannot be null or empty.
     * @param   password  The password to authenticate with. Cannot be null or empty.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    DefaultWardenClient(String endpoint, String username, String password) throws IOException {
        this(WardenService.getInstance(endpoint, 10), username, password);
    }

    /**
     * Creates a new DefaultWardenClient object.
     *
     * @param  service   The Warden service to use. Cannot be null.
     * @param  username  The username to authenticate with. Cannot be null or empty.
     * @param  password  The password to authenticate with. Cannot be null or empty.
     */
    DefaultWardenClient(WardenService service, String username, String password) {
        requireThat(service != null, "The warden service cannot be null.");
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");
        requireThat(password != null && !password.isEmpty(), "Password cannot be null or empty.");
        this.infractions = new InfractionCache();
        this.values = new ValueCache();
        this.service = service;
        this.username = username;
        this.password = password;
        _hostname = _getHostname();
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Throws an illegal argument exception if the condition is not met.
     *
     * @param   condition  The boolean condition to check.
     * @param   message    The exception message.
     *
     * @throws  IllegalArgumentException  If the condition is not met.
     */
    static void requireThat(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    //~ Methods **************************************************************************************************************************************

    @Override
    public void modifyMetric(Policy policy, String username, double delta) throws SuspendedException {
        requireThat(policy != null, "Policy cannot be null.");
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");
        _checkIsSuspended(policy, username);
        _updateLocalValue(policy, username, delta, false);
    }

    @Override
    public void register(List<Policy> policies, int port) throws Exception {
        requireThat(policies != null, "Policy list cannot be null.");
        requireThat(port > 0 && port <= 65535, "Invalid port number.");

        AuthService authService = service.getAuthService();

        authService.login(username, password);
        _initializeUpdaterThread(service);
        _reconcilePolicies(policies);
        _listener = new EventServer(port, infractions);
        _listener.start();
        _subscribeToEvents(port);
    }

    @Override
    public void unregister() throws Exception {
        _unsubscribeFromEvents();
        _listener.close();
        _terminateUpdaterThread();
        service.getAuthService().logout();
    }

    @Override
    public void updateMetric(Policy policy, String username, double value) throws SuspendedException {
        requireThat(policy != null, "Policy cannot be null.");
        requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");
        _checkIsSuspended(policy, username);
        _updateLocalValue(policy, username, value, true);
    }

    private void _checkIsSuspended(Policy policy, String user) throws SuspendedException {
        Infraction infraction = infractions.get(policy.getId(), user);

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

    private void _initializeUpdaterThread(WardenService service) {
        _updater = new MetricUpdater(values, service);
        _updater.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.warn("Uncaught exception in metric updater thread.  Restarting updater thread.", e);
                    _terminateUpdaterThread();
                    _initializeUpdaterThread(service);
                }
            });
        _updater.setDaemon(true);
        _updater.start();
    }

    private List<Policy> _reconcilePolicies(List<Policy> policies) throws IOException {
        PolicyService policyService = service.getPolicyService();
        List<Policy> result = new ArrayList<>(policies.size());

        for (Policy clientPolicy : policies) {
            Policy serverPolicy;
            WardenResponse<Policy> response = policyService.getPolicy(clientPolicy.getService(), clientPolicy.getName());

            if (response.getResources().isEmpty()) {
                List<Policy> toCreate = Arrays.asList(new Policy[] { clientPolicy });

                serverPolicy = policyService.createPolicies(toCreate).getResources().get(0).getEntity();
            } else {
                serverPolicy = response.getResources().get(0).getEntity();
                if (!clientPolicy.equals(serverPolicy)) {
                    clientPolicy.setId(serverPolicy.getId());
                    serverPolicy = policyService.updatePolicy(serverPolicy.getId(), clientPolicy).getResources().get(0).getEntity();
                }
            }
            result.add(serverPolicy);

            List<Resource<Infraction>> suspensionResponses = policyService.getSuspensions(serverPolicy.getId()).getResources();

            for (Resource<Infraction> wardenResponse : suspensionResponses) {
                Infraction suspension = wardenResponse.getEntity();

                infractions.put(suspension);
            }
        }
        return result;
    }

    private void _subscribeToEvents(int port) throws IOException {
        Subscription subscription = new Subscription();

        subscription.setHostname(_hostname);
        subscription.setPort(port);
        _subscription = service.getSubscriptionService().subscribe(subscription).getResources().get(0).getEntity();
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
        service.getSubscriptionService().unsubscribe(_subscription);
    }

    private void _updateLocalValue(Policy policy, String user, Double value, Boolean replace) {
        Double cachedValue = values.get(policy.getId(), user);

        if (cachedValue == null) {
            cachedValue = replace ? value : policy.getDefaultValue() + value;
        } else {
            cachedValue = replace ? value : cachedValue + value;
        }
        values.put(policy.getId(), user, cachedValue);
    }

    //~ Inner Classes ********************************************************************************************************************************

    /**
     * Local cache to store currently suspended users.
     *
     * @author  Tom Valine (tvaline@salesforce.com)
     */
    public static class InfractionCache implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Map<String, Infraction> _infractions;

        /** Creates a new InfractionCache object. */
        public InfractionCache() {
            this._infractions = Collections.synchronizedMap(new LinkedHashMap<String, Infraction>() {

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, Infraction> eldest) {
                        Long expirationTimestamp = eldest.getValue().getExpirationTimestamp();

                        return expirationTimestamp > 0 && expirationTimestamp < System.currentTimeMillis();
                    }
                });
        }

        /**
         * Returns the infraction record for a suspended user.
         *
         * @param   policyId  The ID of the policy to check for. Cannot be null.
         * @param   username  The username to retrieve the infraction record for. Cannot be null or empty.
         *
         * @return  The infraction record or null if the user is not currently suspended.
         */
        public Infraction get(BigInteger policyId, String username) {
            requireThat(policyId != null, "The policy ID cannot be null.");
            requireThat(username != null && !username.isEmpty(), "The username cannot be null or empty.");
            return _infractions.get(createKey(policyId, username));
        }

        /**
         * Indicates whether the infraction cache is empty.
         *
         * @return  True if the cache contains no suspended users.
         */
        public boolean isEmpty() {
            return _infractions.isEmpty();
        }

        /**
         * Inserts an infraction into the cache.
         *
         * @param  infraction  The infraction to insert. Cannot be null.
         */
        public void put(Infraction infraction) {
            requireThat(infraction != null, "The infraction cannot be null.");
            _infractions.put(createKey(infraction.getPolicyId(), infraction.getUsername()), infraction);
        }

        /**
         * Returns the number of records in the cache.
         *
         * @return  The number of records in the cache.
         */
        public int size() {
            return _infractions.size();
        }

        private String createKey(BigInteger policyId, String user) {
            return policyId.toString() + ":" + user;
        }
    }

    /**
     * The local cache used to store usage metrics for users until that time the are written to the server.
     *
     * @author  Tom Valine (tvaline@salesforce.com)
     */
    public static class ValueCache extends HashMap<String, Double> {

        private static final long serialVersionUID = 1L;

        /**
         * Returns the value for a policy and user combination.
         *
         * @param   policyId  The policy ID. Cannot be null.
         * @param   username  The username. Cannot be null or empty.
         *
         * @return  The value of the policy metric for the policy and username combination or null if no value exists.
         */
        public Double get(BigInteger policyId, String username) {
            return get(createKey(policyId, username));
        }

        /**
         * Extracts the value cache key into a list containing the policy ID as a BigInteger and the username as String.
         *
         * @param   key  The ':' delimited value cache key. Cannot be null and must be of the form "id:username";
         *
         * @return  The key components.
         */
        public List<Object> getKeyComponents(String key) {
            requireThat(key != null, "Key cannot be null.");

            String[] components = key.split(":");
            List<Object> result = new ArrayList<>(2);

            result.add(new BigInteger(components[0]));
            result.add(components[1]);
            return result;
        }

        /**
         * Updates the policy value for a specific user.
         *
         * @param  policyId  The policy ID. Cannot be null.
         * @param  user      The username. Cannot be null or empty.
         * @param  value     The new value. Cannot be null.
         */
        public void put(BigInteger policyId, String user, Double value) {
            requireThat(value != null && !value.equals(Double.NaN), "The value cannot be null and must be a real number.");
            put(createKey(policyId, user), value);
        }

        private String createKey(BigInteger policyId, String username) {
            requireThat(policyId != null, "Policy ID cannot be null.");
            requireThat(username != null && !username.isEmpty(), "Username cannot be null or empty.");
            return policyId.toString() + ":" + username;
        }
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
