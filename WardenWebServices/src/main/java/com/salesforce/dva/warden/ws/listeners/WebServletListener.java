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

package com.salesforce.dva.warden.ws.listeners;

import java.util.concurrent.CountDownLatch;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.salesforce.dva.argus.system.SystemMain;

/**
 * Application context listener.
 *
 * @author  Bhinav Sura (bsura@salesforce.com)
 */
public class WebServletListener implements ServletContextListener {

    private static final CountDownLatch _gate = new CountDownLatch(1);
    private static final Logger _logger = LoggerFactory.getLogger(WebServletListener.class);
    private static SystemMain _system;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            _gate.await();
            _logger.info("Stopping Warden web services.");
            _system.getServiceFactory().getMonitorService().stopRecordingCounters();
            _system.getServiceFactory().getSchedulingService().stopAlertScheduling();
            _system.getServiceFactory().getWaaSService().stopPushingMetrics();
            _system.stop();
        } catch (InterruptedException ex) {
            _logger.info("Interrupted while waiting for startup to complete.");
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            _logger.info("Initializing Warden web services.");

            _system = SystemMain.getInstance();

            _system.start();
            _system.getServiceFactory().getManagementService().cleanupRecords();
            _system.getServiceFactory().getSchedulingService().startAlertScheduling();
            _system.getServiceFactory().getMonitorService().startRecordingCounters();
            _system.getServiceFactory().getWaaSService().startPushingMetrics();
        } finally {
            _gate.countDown();
        }
    }

    /**
     * Returns the system main instance.
     *
     * @return  The system main instance or null if the system was interrupted during initialization.
     */
    public static SystemMain getSystem() {
        try {
            _gate.await();

            return _system;
        } catch (InterruptedException ex) {
            _logger.warn("Interrupted while waiting for startup to complete.");

            return null;
        }
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



