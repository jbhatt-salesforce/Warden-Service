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
	 
package com.salesforce.dva.argus.service.warden;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.salesforce.dva.argus.entity.Dashboard;
import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.argus.inject.SLF4JTypeListener;
import com.salesforce.dva.argus.service.AlertService;
import com.salesforce.dva.argus.service.AnnotationService;
import com.salesforce.dva.argus.service.AuditService;
import com.salesforce.dva.argus.service.DashboardService;
import com.salesforce.dva.argus.service.MetricService;
import com.salesforce.dva.argus.service.MonitorService;
import com.salesforce.dva.argus.service.ServiceManagementService;
import com.salesforce.dva.argus.service.UserService;
import com.salesforce.dva.argus.service.WardenService;
import com.salesforce.dva.argus.service.jpa.DefaultJPAService;
import com.salesforce.dva.argus.system.SystemConfiguration;
import org.slf4j.Logger;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.persistence.EntityManager;

import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

/**
 * Default implementation of the warden service.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 * @author  Bhinav Sura (bhinav.sura@salesforce.com)
 */
@Singleton
public class DummyWardenService extends DefaultJPAService implements WardenService {

    //~ Instance fields ******************************************************************************************************************************

    private ScheduledExecutorService _scheduledExecutorService;
    @SLF4JTypeListener.InjectLogger
    private Logger _logger;
    @Inject
    private Provider<EntityManager> emf;
    private final AlertService _alertService;
    private final MonitorService _monitorService;
    private final UserService _userService;
    private final MetricService _metricService;
    private final ServiceManagementService _serviceManagementRecordService;
    private final DashboardService _dashboardService;
    private final AnnotationService _annotationService;
    private final PrincipalUser _adminUser;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new DefaultWardenService object.
     *
     * @param  alertService              The alert service user to create warden alerts when necessary. Cannot be null.
     * @param  monitorService            The monitor service used to collect warden policy metric counters. Cannot be null.
     * @param  userService               The user service. Cannot be null.
     * @param  metricService             The metric service. Cannot be null.
     * @param  serviceManagementService  The service management service. Cannot be null.
     * @param  dashboardService          The dashboard service. Cannot be null.
     * @param  auditService              The audit service. Cannot be null.
     * @param  annotationService         The annotation service. Cannot be null.
     * @param _sysConfig Service properties
     */
    @Inject
    protected DummyWardenService(AlertService alertService, MonitorService monitorService, UserService userService, MetricService metricService,
        ServiceManagementService serviceManagementService, DashboardService dashboardService, AuditService auditService,
        AnnotationService annotationService, SystemConfiguration _sysConfig) {
        super(auditService, _sysConfig);
        requireArgument(alertService != null, "Alert service cannot be null.");
        requireArgument(monitorService != null, "Monitor service cannot be null.");
        requireArgument(userService != null, "User service cannot be null.");
        requireArgument(metricService != null, "Metric service cannot be null.");
        requireArgument(serviceManagementService != null, "Service management service cannot be null.");
        requireArgument(dashboardService != null, "Dashboard service cannot be null.");
        requireArgument(annotationService != null, "Annotation service cannot be null.");
        _alertService = alertService;
        _monitorService = monitorService;
        _userService = userService;
        _metricService = metricService;
        _serviceManagementRecordService = serviceManagementService;
        _dashboardService = dashboardService;
        _annotationService = annotationService;
        _adminUser = null;
        _scheduledExecutorService = null;
    }

    //~ Methods **************************************************************************************************************************************

    @Override
    public void dispose() {
    }

    @Override
    @Transactional
    public void updatePolicyCounter(PrincipalUser user, PolicyCounter counter, double value) {
    }

    @Override
    @Transactional
    public double modifyPolicyCounter(PrincipalUser user, PolicyCounter counter, double delta) {
    	return 0.0;
    }

    @Override
    @Transactional
    public void assertSubSystemUsePermitted(PrincipalUser user, SubSystem subSystem) {
    }

    @Override
    @Transactional
    public boolean suspendUser(PrincipalUser user, SubSystem subSystem) {
    	return false;
    }

    @Override
    @Transactional
    public void reinstateUser(PrincipalUser user, SubSystem subSystem) {
    }

    @Override
    @Transactional
    public void updatePolicyLimitForUser(PrincipalUser user, PolicyCounter counter, double value) {
    }

    @Override
    @Transactional
    public void updateSuspensionLevel(SubSystem subSystem, int level, long durationInMillis) {
    }

    @Override
    @Transactional
    public void updateSuspensionLevels(SubSystem subSystem, Map<Integer, Long> levels) {
    }

    @Override
    @Transactional
    public void enableWarden() {
    }

    @Override
    @Transactional
    public void disableWarden() {
    }

    @Override
    @Transactional
    public Dashboard getWardenDashboard(PrincipalUser user) {
    	return null;
    }

    /**
     * Indicates if the warden service is enabled.
     *
     * @return  True if the service is enabled.
     */
    public boolean isWardenServiceEnabled() {
    	return false;
    }
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
