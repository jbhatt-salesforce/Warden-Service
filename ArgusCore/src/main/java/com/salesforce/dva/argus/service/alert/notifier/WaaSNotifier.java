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
package com.salesforce.dva.argus.service.alert.notifier;

import com.google.inject.Inject;
import com.salesforce.dva.argus.entity.Infraction;
import com.salesforce.dva.argus.entity.Policy;
import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.argus.entity.SuspensionLevel;
import com.salesforce.dva.argus.service.AnnotationService;
import com.salesforce.dva.argus.service.MetricService;
import com.salesforce.dva.argus.service.UserService;
import com.salesforce.dva.argus.service.WaaSService;
import com.salesforce.dva.argus.service.alert.DefaultAlertService.NotificationContext;
import com.salesforce.dva.argus.service.metric.MetricReader.TimeUnit;
import com.salesforce.dva.argus.service.warden.DefaultWaaSService;
import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;
import com.salesforce.dva.argus.system.SystemConfiguration;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the warden as a service notifier. Writes an infraction and suspends the user if required.
 *
 * @author Ruofan Zhang (rzhang@salesforce.com)
 */
public class WaaSNotifier extends DefaultNotifier {

    private final WaaSService _waaSService;
    private final UserService _userService;

    /**
     * Constructs ...
     *
     *
     * @param userService
     * @param waaSService
     * @param metricService
     * @param annotationService
     * @param systemConfiguration
     */
    @Inject
    public WaaSNotifier(UserService userService, WaaSService waaSService, MetricService metricService, AnnotationService annotationService,
            SystemConfiguration systemConfiguration) {
        super(metricService, annotationService, systemConfiguration);
        requireArgument((_waaSService = waaSService) != null, "WaaS service cannot be null.");
        requireArgument((_userService = userService) != null, "User service cannot be null.");
    }

    @Override
    protected void clearAdditionalNotification(NotificationContext context) {
    }

    @Override
    protected void sendAdditionalNotification(NotificationContext context) {
        try {
            String name = context.getAlert().getName().replace(DefaultWaaSService.ALERT_PREFIX, "");
            String[] policyIdAndUsername = name.split("\\.", 2);
            BigInteger policyId = new BigInteger(policyIdAndUsername[0]);
            String username = policyIdAndUsername[1];
            Policy policy = _waaSService.getPolicy(policyId);
            PrincipalUser admin = _userService.findAdminUser();
            PrincipalUser user = _userService.findUserByUsername(username);
            String timeUnit = policy.getTimeUnit();
            TimeUnit unit = TimeUnit.fromString(policy.getTimeUnit().substring(policy.getTimeUnit().length() - 1));
            long offset = Long.valueOf(timeUnit.substring(0, timeUnit.length())) * unit.getValue();
            List<Infraction> infractions = _waaSService.getInfractionsByPolicyAndUsername(policyId, username);
            Long count = infractions.stream().filter(i -> i.getInfractionTimestamp() >= context.getTriggerFiredTime() - offset).collect(Collectors.counting())
                    + 1;
            List<SuspensionLevel> levels = policy.getSuspensionLevels().stream().filter(l -> l.getInfractionCount() <= count).sorted((SuspensionLevel o1, SuspensionLevel o2)
                    -> Integer.compare(o1.getInfractionCount(), o2.getInfractionCount())).collect(Collectors.toList());
            Long suspensionTime = null;
            if (!levels.isEmpty()) {
                SuspensionLevel level = levels.get(levels.size() - 1);
                suspensionTime = level.getSuspensionTime() == -1 ? -1 : level.getSuspensionTime() + context.getTriggerFiredTime();
            }
            Infraction infraction = new Infraction(admin, policy, user, context.getTriggerFiredTime(), suspensionTime, Double.valueOf(context.getTriggerEventValue()));
            infraction.setModifiedBy(admin);
            _waaSService.updateInfraction(infraction);
        } catch (Exception ex) {
            LoggerFactory.getLogger(WaaSNotifier.class).warn("Failed to record infraction");
        }
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
