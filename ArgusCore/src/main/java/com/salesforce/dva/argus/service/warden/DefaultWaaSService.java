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

package com.salesforce.dva.argus.service.warden;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.salesforce.dva.argus.entity.Alert;
import com.salesforce.dva.argus.entity.Infraction;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.entity.Notification;
import com.salesforce.dva.argus.entity.Policy;
import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.argus.entity.SuspensionLevel;
import com.salesforce.dva.argus.entity.Trigger;
import com.salesforce.dva.argus.entity.Trigger.TriggerType;
import com.salesforce.dva.argus.inject.SLF4JTypeListener;
import com.salesforce.dva.argus.service.AlertService;
import com.salesforce.dva.argus.service.AuditService;
import com.salesforce.dva.argus.service.MetricService;
import com.salesforce.dva.argus.service.MonitorService;
import com.salesforce.dva.argus.service.UserService;
import com.salesforce.dva.argus.service.WaaSService;
import com.salesforce.dva.argus.service.alert.notifier.WaaSNotifier;
import com.salesforce.dva.argus.service.jpa.DefaultJPAService;
import com.salesforce.dva.argus.system.SystemConfiguration;
import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

/**
 * Provides the functionality for Warden as a service.
 *
 * @author         Ruofan Zhang (rzhang@salesforce.com)
 * @author         Tom Valine (tvaline@salesforce.com)
 */
@Singleton
public class DefaultWaaSService extends DefaultJPAService implements WaaSService {

    public final static String ALERT_PREFIX = "__waas-";
    private static final long ALERT_UPDATE_INTERVAL_MS = 15 * 60 * 1000;
    private static final long ALERT_AUDIT_INTERVAL_MS = 24 * 60 * 60 * 1000;
    @SLF4JTypeListener.InjectLogger
    private Logger _logger;
    @Inject
    private Provider<EntityManager> emf;
    private final MonitorService _monitorService;
    private final MetricService _metricService;
    private final AlertService _alertService;
    private final UserService _userService;
    private final ScheduledExecutorService _executorService;
    private final PrincipalUser _admin;
    private final Map<String, Long> _alertsToUpdate;

    /**
     * Creates a new instance of DefaultWaaSService.
     *
     * @param auditService The audit service to use.  Cannot be null.
     * @param config The system configuration.  Cannot be null.
     * @param monitorService The monitor service.  Cannot be null.
     * @param alertService The alert service.  Cannot be null.
     * @param metricService The metric service. Cannot be null.
     * @param userService The user service.  Cannot be null.
     */
    @Inject
    protected DefaultWaaSService(AuditService auditService, SystemConfiguration config, MonitorService monitorService, MetricService metricService,
                                 AlertService alertService, UserService userService) {
        super(auditService, config);

        requireArgument((_monitorService = monitorService) != null, "Collection service cannot be null.");
        requireArgument((_metricService = metricService) != null, "Metric service cannot be null.");
        requireArgument((_alertService = alertService) != null, "Alert service cannot be null.");
        requireArgument((_userService = userService) != null, "User service cannot be null.");

        _executorService = _startAlertAuditor();
        _admin = _userService.findAdminUser();
        _alertsToUpdate = Collections.synchronizedMap(new LinkedHashMap<String, Long>() {

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                return eldest.getValue() <= System.currentTimeMillis() - ALERT_UPDATE_INTERVAL_MS;
            }

        });
    }

    private Notification _createNotification(Alert alert, Policy policy, String userName, Trigger trigger) {
        String name = ALERT_PREFIX + constructMetricName(policy.getId(), userName);
        Notification notification = new Notification(name, alert, WaaSNotifier.class.getName(), Collections.<String>emptyList(), 0L);

        notification.setModifiedBy(_admin);
        notification.setCreatedBy(_admin);
        notification.setTriggers(Arrays.asList(new Trigger[] { trigger }));
        notification.setSubscriptions(policy.getOwners());

        return notification;
    }

    private Trigger _createTrigger(Alert alert, Policy policy, String userName) {
        TriggerType type = TriggerType.fromString(policy.getTriggerType().name());
        String name = ALERT_PREFIX + constructMetricName(policy.getId(), userName);
        List<Double> thresholds = policy.getThresholds();
        Double threshold = thresholds.get(0);
        Double secondaryThreshold = (thresholds.size() > 1) ? thresholds.get(1) : null;
        Long inertia = 0L;
        Trigger trigger = new Trigger(alert, type, name, threshold, secondaryThreshold, inertia);

        trigger.setCreatedBy(_admin);
        trigger.setModifiedBy(_admin);

        return trigger;
    }

    private void _deleteAlerts(BigInteger policyId) {
        String prefix = MessageFormat.format("{0}{1}-", ALERT_PREFIX, policyId.toString());

        _alertService.findAlertsByNameWithPrefix(prefix).stream().forEach(a -> {
                _alertService.deleteAlert(a);
            } );
    }

    private ScheduledExecutorService _startAlertAuditor() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        service.scheduleAtFixedRate(
            () -> {
                List<Alert> alerts = _alertService.findAlertsByNameWithPrefix(ALERT_PREFIX);

                alerts.stream()
                      .filter((alert) -> (alert.isEnabled()))
                      .forEach(
                          (alert) -> {
                              try {
                                  List<Metric> metrics = _metricService.getMetrics(alert.getExpression());
                                  Metric metric = metrics.isEmpty() ? null : metrics.get(0);

                                  if ((metric == null) || metric.getDatapoints().isEmpty()) {
                                      _logger.info("Disabling warden alert:{}", alert.getName());
                                      alert.setEnabled(false);
                                      _alertService.updateAlert(alert);
                                  }
                              } catch (Exception ex) {
                                  _logger.warn("Failed to get metrics for alert: {}. Reason: {}", alert, ex.getMessage());
                              }
                          } );
            } ,
            0L,
            ALERT_AUDIT_INTERVAL_MS,
            TimeUnit.MILLISECONDS);
        service.scheduleAtFixedRate(
            () -> {
                _alertsToUpdate.keySet().stream().forEach((k) -> {
                        String[] split = k.split("\\.", 2);
                        String policyId = split[0];
                        String userName = split[1];

                        try {
                            _updateAlert(new BigInteger(policyId), userName);
                        } catch (Exception ex) {
                            _logger.warn("Failed to update alert for policy {} and userName {}. Reason: {}", policyId, userName, ex.getMessage());
                        }
                    } );
            } ,
            0L,
            ALERT_UPDATE_INTERVAL_MS,
            TimeUnit.MILLISECONDS);

        service.scheduleAtFixedRate(()->{
            Infraction.deleteExpired(emf.get());
        }, 12L, 24L, TimeUnit.HOURS);
        return service;
    }

    private void _updateAlert(BigInteger policyId, String userName) {
        String alertName = ALERT_PREFIX + constructMetricName(policyId, userName);
        Policy policy = getPolicy(policyId);
        Alert alert = Alert.findByNameAndOwner(emf.get(), alertName, _admin);
        String expression = constructMetricExpression(policyId, userName, "-".concat(policy.getTimeUnit()), "-0m");

        if (alert == null) {
            alert = new Alert(_admin, _admin, alertName, expression, policy.getCronEntry());
        } else {
            alert.getNotifications().stream().forEach(n -> _alertService.deleteNotification(n));
            alert.getTriggers().stream().forEach(t -> _alertService.deleteTrigger(t));
        }

        Trigger trigger = _createTrigger(alert, policy, userName);
        Notification notification = _createNotification(alert, policy, userName, trigger);

        alert.setEnabled(true);
        alert.setMissingDataNotificationEnabled(false);
        alert.setTriggers(Arrays.asList(new Trigger[] { trigger }));
        alert.setNotifications(Arrays.asList(new Notification[] { notification }));
        _alertService.updateAlert(alert);
    }

    /**
     * Helper method to construct a metric expression from the policy ID and userName.
     *
     * @param policyId The policy ID.  Cannot be null.
     * @param userName The userName. Cannot be null or empty.
     * @param start The optional start time.  Can be relative or absolute timestamp.  If not specified it defaults to '-1d'.
     * @param end The optional end time.  Can be relative or absolute timestamp.  If not specified it defaults to '-0d'.
     * @return The corresponding metric query expression.
     */
    @Override
    public String constructMetricExpression(BigInteger policyId, String userName, String start, String end) {
        Policy policy = getPolicy(policyId);
        String aggregator = policy.getAggregator().name().toLowerCase();
        String name = constructMetricName(policyId, userName);

        return MessageFormat.format("{0}:{1}:argus.custom:{2}:{3}", start, end, name, aggregator);
    }

    /**
     * Helper method to construct the metric name given the policy ID and userName.
     * @param policyId The policy ID.  Cannot be null.
     * @param userName The userName.  Cannot be null or empty.
     * @return The corresponding metric name.
     */
    @Override
    public String constructMetricName(BigInteger policyId, String userName) {
        return policyId.toString() + "." + userName;
    }

    @Override
    @Transactional
    public void deleteInfraction(BigInteger infractionId) {
        requireNotDisposed();
        requireArgument((infractionId != null) && (infractionId.signum() >= 0), "Invalid infraction ID.");

        EntityManager em = emf.get();

        deleteEntity(em, infractionId, Infraction.class);
        _logger.debug("Deleted infraction {}.", infractionId);
        em.flush();
    }

    @Override
    @Transactional
    public void deletePolicy(BigInteger policyId) {
        requireNotDisposed();
        requireArgument((policyId != null) && (policyId.signum() >= 0), "Invalid policy ID.");

        EntityManager em = emf.get();

        deleteEntity(em, policyId, Policy.class);
        _deleteAlerts(policyId);
        _logger.debug("Deleted policy {}.", policyId);
        em.flush();
    }

    @Override
    @Transactional
    public void deleteSuspensionLevel(BigInteger suspensionLevelId) {
        requireNotDisposed();
        requireArgument((suspensionLevelId != null) && (suspensionLevelId.signum() >= 0), "Invalid suspension level ID.");

        EntityManager em = emf.get();

        deleteEntity(em, suspensionLevelId, SuspensionLevel.class);
        _logger.debug("Deleted suspension level {}.", suspensionLevelId);
        em.flush();
    }

    @Override
    public void dispose() {
        super.dispose();

        if (_executorService != null) {
            try {
                _executorService.shutdownNow();

                if (!_executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    _logger.warn("Termination of alert auditor thread timed out.");
                }
            } catch (InterruptedException ex) {
                _logger.warn("Interrupted while shutting down the alert auditor thread.");
            }
        }
    }

    @Override
    @Transactional
    public Infraction updateInfraction(Infraction infraction) {
        requireNotDisposed();
        requireArgument(infraction != null, "Invalid infraction.");

        EntityManager em = emf.get();
        Infraction result = mergeEntity(em, infraction);

        _logger.debug("Updated infraction to : {}", result);
        em.flush();

        return result;
    }

    @Override
    @Transactional
    public void updateMetric(BigInteger policyId, String userName, Double value) {
        requireNotDisposed();
        requireArgument((policyId != null) && (policyId.signum() >= 0), "Invalid policy ID.");
        requireArgument((userName != null) &&!userName.isEmpty(), "Invalid userName.");
        requireArgument((value != null) &&!value.isInfinite() &&!value.isNaN(), "Invalid value.");
        _monitorService.updateCustomCounter(constructMetricName(policyId, userName), value, Collections.<String, String>emptyMap());
        _alertsToUpdate.put(constructMetricName(policyId, userName), System.currentTimeMillis());
    }

    @Override
    @Transactional
    public Policy updatePolicy(Policy policy) {
        requireNotDisposed();
        requireArgument(policy != null, "Invalid policy.");

        EntityManager em = emf.get();
        Policy result = mergeEntity(em, policy);

        _logger.debug("Updated policy to : {}", result);
        em.flush();

        return result;
    }

    @Override
    @Transactional
    public SuspensionLevel updateSuspensionLevel(SuspensionLevel suspensionLevel) {
        requireNotDisposed();
        requireArgument(suspensionLevel != null, "Invalid suspension level.");

        EntityManager em = emf.get();
        SuspensionLevel result = mergeEntity(em, suspensionLevel);

        _logger.debug("Updated suspension level to : {}", result);
        em.flush();

        return result;
    }

    @Override
    @Transactional
    public Infraction getInfraction(BigInteger infractionId) {
        requireNotDisposed();
        requireArgument((infractionId != null) && (infractionId.signum() >= 0), "Invalid infraction ID.");

        Infraction result = findEntity(emf.get(), infractionId, Infraction.class);

        _logger.debug("Query for infraction having id {} returned {}.", infractionId, result);

        return result;
    }

    @Override
    @Transactional
    public List<Infraction> getInfractionsByPolicyAndUserName(BigInteger policyId, String userName) {
        requireNotDisposed();
        requireArgument((policyId != null) && (policyId.signum() >= 0), "Invalid policy ID.");
        requireArgument((userName != null) &&!userName.isEmpty(), "Invalid userName.");

        EntityManager em = emf.get();
        List<Infraction> result = Infraction.findByPolicyAndUserName(em, policyId, userName);

        _logger.debug("Query for infractions for policy {} and {} returned {}.", policyId, userName, result);

        return result;
    }

    @Override
    @Transactional
    public List<Infraction> getInfractionsByUserName(String userName) {
        requireNotDisposed();
        requireArgument((userName != null) &&!userName.isEmpty(), "Invalid userName.");

        EntityManager em = emf.get();
        List<Infraction> result = Infraction.findByUserName(em, userName);

        _logger.debug("Query for infractions for {} returned {}.", userName, result);

        return result;
    }

    @Override
    @Transactional
    public List<Infraction> getInfractionsForPolicy(BigInteger policyId) {
        requireNotDisposed();
        requireArgument((policyId != null) && (policyId.signum() >= 0), "Invalid policy ID.");

        EntityManager em = emf.get();
        List<Infraction> result = Infraction.findByPolicy(em, policyId);

        _logger.debug("Query for infractions for policy {} returned {}.", policyId, result);

        return result;
    }

    @Override
    @Transactional
    public List<Metric> getMetrics(BigInteger policyId, String userName, String start, String end) {
        requireArgument((policyId != null) && (policyId.signum() >= 0), "Invalid policy ID.");
        requireArgument((userName != null) &&!userName.isEmpty(), "Invalid userName.");

        if (start == null) {
            start = "-1d";
        }

        if (end == null) {
            end = "-0d";
        }

        String expression = constructMetricExpression(policyId, userName, start, end);
        List<Metric> result = _metricService.getMetrics(expression);

        _logger.debug("Query for metrics for policy {} and {} from {} to {} returned {}.", policyId, userName, start, end, result);

        return result;
    }

    @Override
    @Transactional
    public List<Policy> getPolicies() {
        requireNotDisposed();

        EntityManager em = emf.get();
        List<Policy> result = Policy.findAll(em);

        _logger.debug("Query for all policies returned {} policies.", result.size());

        return result;
    }

    @Override
    @Transactional
    public List<Policy> getPoliciesForUserName(String userName) {
        requireNotDisposed();
        requireArgument((userName != null) &&!userName.isEmpty(), "Invalid userName.");

        EntityManager em = emf.get();
        List<Policy> result = Policy.findByUserName(em, userName);

        _logger.debug("Query for all policies for {} returned {} policies.", userName, result.size());

        return result;
    }

    @Override
    @Transactional
    public Policy getPolicy(BigInteger policyId) {
        requireNotDisposed();
        requireArgument((policyId != null) && (policyId.signum() >= 0), "ID must be a positive non-zero value.");

        Policy result = findEntity(emf.get(), policyId, Policy.class);

        _logger.debug("Query for policy having id {} returned {}.", policyId, result);

        return result;
    }

    @Override
    @Transactional
    public Policy getPolicyByNameAndService(String name, String service) {
        requireNotDisposed();
        requireArgument((name != null) &&!name.isEmpty(), "Invalid policy name.");
        requireArgument((service != null) &&!service.isEmpty(), "Invalid service name.");

        EntityManager em = emf.get();
        Policy result = Policy.findByNameAndService(em, name, service);

        _logger.debug("Query for policy returned {}.", result);

        return result;
    }

    @Override
    @Transactional
    public SuspensionLevel getSuspensionLevelForPolicy(BigInteger policyId, BigInteger suspensionLevelId) {
        requireNotDisposed();
        requireArgument((policyId != null) && (policyId.signum() >= 0), "Invalid policy ID.");
        requireArgument((suspensionLevelId != null) && (suspensionLevelId.signum() >= 0), "Invalid suspension level ID.");

        EntityManager em = emf.get();
        SuspensionLevel result = SuspensionLevel.findByPolicyAndLevel(em, policyId, suspensionLevelId);

        return result;
    }

    @Override
    @Transactional
    public List<Infraction> getSuspensionsByUserName(String userName) {
        requireNotDisposed();
        requireArgument((userName != null) &&!userName.isEmpty(), "Invalid userName.");

        EntityManager em = emf.get();
        List<Infraction> result = Infraction.findSuspensionsByUserName(em, userName);

        return result;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
