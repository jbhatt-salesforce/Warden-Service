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
     
package com.salesforce.dva.argus.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.curator.test.TestingServer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.matcher.Matchers;
import com.salesforce.dva.argus.AbstractTest;
import com.salesforce.dva.argus.entity.Alert;
import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.argus.entity.Trigger;
import com.salesforce.dva.argus.inject.SLF4JTypeListener;
import com.salesforce.dva.argus.service.WardenService.PolicyCounter;
import com.salesforce.dva.argus.service.WardenService.SubSystem;
import com.salesforce.dva.argus.service.annotation.DefaultAnnotationService;
import com.salesforce.dva.argus.service.audit.DefaultAuditService;
import com.salesforce.dva.argus.service.collect.DefaultCollectionService;
import com.salesforce.dva.argus.service.history.DefaultHistoryService;
import com.salesforce.dva.argus.service.jpa.DefaultDashboardService;
import com.salesforce.dva.argus.service.jpa.DefaultGlobalInterlockService;
import com.salesforce.dva.argus.service.jpa.DefaultNamespaceService;
import com.salesforce.dva.argus.service.jpa.DefaultServiceManagementService;
import com.salesforce.dva.argus.service.jpa.DefaultUserService;
import com.salesforce.dva.argus.service.management.DefaultManagementService;
import com.salesforce.dva.argus.service.metric.DefaultMetricService;
import com.salesforce.dva.argus.service.monitor.DefaultMonitorService;
import com.salesforce.dva.argus.service.schema.DefaultDiscoveryService;
import com.salesforce.dva.argus.service.tsdb.CachedTSDBService;
//import com.salesforce.dva.argus.service.warden.DefaultWaaSService;
import com.salesforce.dva.argus.service.warden.DefaultWardenService;
import com.salesforce.dva.argus.system.SystemConfiguration;
import com.salesforce.dva.argus.system.SystemException;
import com.salesforce.dva.argus.system.SystemMain;
import com.salesforce.dva.argus.system.SystemConfiguration.Property;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;

public class WardenServiceTest extends AbstractTest {

    private UserService _userService;
    private WardenService _wardenService;
    private AlertService _alertService;  
    
    @Before
    @Override
    public void setUp() {
    	 try {
             Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
             DriverManager.getConnection("jdbc:derby:memory:argus;create=true").close();
             zkTestServer = new TestingServer(2185);
         } catch (Exception ex) {
             LoggerFactory.getLogger(getClass()).error("Exception in setUp:{}", ex.getMessage());
             fail("Exception during database startup.");
         }
         setupEmbeddedKafka();
         system = getInstance();
         system.start();
       
        _userService = system.getServiceFactory().getUserService();        
        _alertService = system.getServiceFactory().getAlertService();
        _wardenService = system.getServiceFactory().getWardenService();

        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");

        if (user == null) {
            user = new PrincipalUser("bhinav.sura", "bhinav.sura@salesforce.com");
            user = _userService.updateUser(user);
        }
        _wardenService.reinstateUser(user, SubSystem.API);
        _wardenService.reinstateUser(user, SubSystem.POSTING);

        SubSystem[] subSystems = SubSystem.values();

        for (SubSystem ss : subSystems) {
            Map<Integer, Long> levels = new HashMap<>();

            levels.put(1, 10 * 60 * 1000L);
            levels.put(2, 30 * 60 * 1000L);
            levels.put(3, 60 * 60 * 1000L);
            levels.put(4, 10 * 60 * 60 * 1000L);
            levels.put(5, 24 * 60 * 60 * 1000L);
            _wardenService.updateSuspensionLevels(ss, levels);
        }
    }
    
    @Test
    public void testServiceIsSingleton() {
        assertTrue(_wardenService == system.getServiceFactory().getWardenService());
    }

    @Test
    public void testSuspendAdminUser() {
        assertFalse(_wardenService.suspendUser(_userService.findAdminUser(), SubSystem.POSTING));
    }

    @Test
    public void testSuspendUser() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");
        boolean isIndefinitelySuspended = _wardenService.suspendUser(user, SubSystem.API);

        assertFalse(isIndefinitelySuspended);
    }

    @Test
    public void testSuspendUserIndefinitely() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");
        boolean isIndefinitelySuspended = true;

        for (int i = 0; i < 14; i++) {
            int index = random.nextInt(SubSystem.values().length);
            SubSystem subSystem = SubSystem.values()[index];

            isIndefinitelySuspended = _wardenService.suspendUser(user, subSystem);
        }
        assertFalse(isIndefinitelySuspended);
        isIndefinitelySuspended = _wardenService.suspendUser(user, SubSystem.API);
        assertTrue(isIndefinitelySuspended);
        isIndefinitelySuspended = _wardenService.suspendUser(user, SubSystem.API);
        assertTrue(isIndefinitelySuspended);
    }

    @Test
    public void testAssertSubsystemUsePermitted_AdminUser() {
        _wardenService.assertSubSystemUsePermitted(_userService.findAdminUser(), SubSystem.API);
        assertTrue(true);
    }

    @Test
    public void testAssertSubsystemUsePermitted_NoSuspension() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");

        _wardenService.assertSubSystemUsePermitted(user, SubSystem.API);
        assertTrue(true);
    }

    @Test(expected = SystemException.class)
    public void testAssertSubsystemUsePermitted_IndefiniteSuspension() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");

        for (int i = 0; i < 15; i++) {
            int index = random.nextInt(SubSystem.values().length);
            SubSystem subSystem = SubSystem.values()[index];

            _wardenService.suspendUser(user, subSystem);
        }
        _wardenService.assertSubSystemUsePermitted(user, SubSystem.API);
    }

    @Test(expected = SystemException.class)
    public void testAssertSubsystemUsePermitted_NonIndefiniteSuspension() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");

        _wardenService.suspendUser(user, SubSystem.API);
        _wardenService.assertSubSystemUsePermitted(user, SubSystem.API);
    }

    @Test
    public void testAssertSubsystemUsePermitted_ExpiredSuspension() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");

        _wardenService.updateSuspensionLevel(SubSystem.API, 1, 5 * 1000L);
        _wardenService.suspendUser(user, SubSystem.API);
        try {
            Thread.sleep(6 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _wardenService.assertSubSystemUsePermitted(user, SubSystem.API);
        assertTrue(true);
    }

    @Test
    public void testUpdatePolicyCounterEnablesAlert() {
        _wardenService.updatePolicyCounter(_userService.findAdminUser(), PolicyCounter.METRICS_PER_HOUR, new Random().nextInt(50));

        String alertName = "";

        try {
            Method method = DefaultWardenService.class.getDeclaredMethod("_constructWardenAlertName", PrincipalUser.class, PolicyCounter.class);

            method.setAccessible(true);
            alertName = (String) method.invoke(_wardenService, _userService.findAdminUser(), PolicyCounter.METRICS_PER_HOUR);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new SystemException("Failed to construct alert name using reflection");
        }

        Alert alert = _alertService.findAlertByNameAndOwner(alertName, _userService.findAdminUser());

        assertTrue(alert.isEnabled());
        _alertService.deleteAlert(alert);
    }

    @Test
    public void testModifyPolicyCounterEnablesAlert() {
        _wardenService.modifyPolicyCounter(_userService.findAdminUser(), PolicyCounter.METRICS_PER_HOUR, new Random().nextInt(50));

        String alertName = "";

        try {
            Method method = DefaultWardenService.class.getDeclaredMethod("_constructWardenAlertName", PrincipalUser.class, PolicyCounter.class);

            method.setAccessible(true);
            alertName = (String) method.invoke(_wardenService, _userService.findAdminUser(), PolicyCounter.METRICS_PER_HOUR);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new SystemException("Failed to construct alert name using reflection");
        }

        Alert alert = _alertService.findAlertByNameAndOwner(alertName, _userService.findAdminUser());

        assertTrue(alert.isEnabled());
        _alertService.deleteAlert(alert);
    }

    @Test
    public void testSubSystemSuspensionLevels() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");

        _wardenService.updateSuspensionLevel(SubSystem.POSTING, 5, 5 * 1000L);
        for (int i = 0; i < 6; i++) {
            _wardenService.suspendUser(user, SubSystem.POSTING);
        }
        try {
            Thread.sleep(6 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _wardenService.assertSubSystemUsePermitted(user, SubSystem.POSTING);
        assertTrue(true);
    }

    @Test
    public void testWardenAlertUsesUpdatedPolicyLimitForUser() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");

        _wardenService.updatePolicyLimitForUser(user, PolicyCounter.METRICS_PER_HOUR, 200);
        _wardenService.updatePolicyCounter(user, PolicyCounter.METRICS_PER_HOUR, 50);

        String alertName = "";

        try {
            Method method = DefaultWardenService.class.getDeclaredMethod("_constructWardenAlertName", PrincipalUser.class, PolicyCounter.class);

            method.setAccessible(true);
            alertName = (String) method.invoke(_wardenService, user, PolicyCounter.METRICS_PER_HOUR);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new SystemException("Failed to construct alert name using reflection");
        }

        Alert alert = _alertService.findAlertByNameAndOwner(alertName, _userService.findAdminUser());
        Trigger trigger = alert.getTriggers().get(0);

        assertEquals(Double.valueOf(200), trigger.getThreshold());
    }

    @Test
    public void testEnableWarden() {
        _wardenService.enableWarden();
        assertTrue(_wardenService.isWardenServiceEnabled());
    }

    @Test
    public void testDisableWarden() {
        _wardenService.disableWarden();
        assertFalse(_wardenService.isWardenServiceEnabled());
    }

    @Test
    public void testWardenDashboard() {
        PrincipalUser user = _userService.findUserByUsername("bhinav.sura");

        assertNotNull(_wardenService.getWardenDashboard(user));
    }
    class WardenTestModule extends AbstractModule{
    	//~ Instance fields ******************************************************************************************************************************

        private final Properties _config;
        private SystemConfiguration _systemConfiguration;

        //~ Constructors *********************************************************************************************************************************

        /**
         * Creates a new SystemInitializer object.
         *
         * @param  config  The configuration used to initialize the system.
         */
        WardenTestModule(Properties config) {
            if (config == null) {
                config = readConfigInfo();
            }
            _config = config;
        }

        //~ Methods **************************************************************************************************************************************

        private void readClasspath(Properties props, String path) {
            if ((path != null) && !path.isEmpty()) {
                InputStream is = null;
                Properties result = new Properties();

                try {
                    is = SystemConfiguration.class.getResourceAsStream(path);
                    result.load(is);
                    props.putAll(result);
                } catch (IOException ex) {
                    throw new SystemException(ex);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            assert false : "This should never occur.";
                        }
                    }
                }
            }
        }

        private Properties readConfigInfo() {
            Properties props = new Properties();

            readFile(props, System.getProperty("argus.config.public.location"));
            readFile(props, System.getProperty("argus.config.private.location"));
            readClasspath(props, "/META-INF/build.properties");
            return props;
        }

        private void readFile(Properties props, String filePath) {
            if ((filePath != null) && !filePath.isEmpty()) {
                InputStream is = null;
                Properties result = new Properties();

                try {
                    is = new FileInputStream(filePath);
                    result.load(is);
                    props.putAll(result);
                } catch (IOException ex) {
                    LoggerFactory.getLogger("com.salesforce.dva.argus").warn("Unable to load properties file \"{}\". Reason: {}", filePath,
                        ex.getMessage());
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            assert false : "This should never occur.";
                        }
                    }
                }
            }
        }

        //~ Methods **************************************************************************************************************************************

        /** @see  AbstractModule#configure() */
        @Override
        protected void configure() {
            configureLogging();
            configureSystem();
            configurePersistence();
            configureServices();
        }

        private void configureSystem() {
            _systemConfiguration = new SystemConfiguration(_config);

            Logger app = Logger.class.cast(LoggerFactory.getLogger("com.salesforce.dva.argus"));

            app.setLevel(Level.toLevel(_systemConfiguration.getValue(SystemConfiguration.Property.LOG_LEVEL)));
            bind(SystemConfiguration.class).toInstance(_systemConfiguration);
            bindListener(Matchers.any(), new SLF4JTypeListener());
            _systemConfiguration.putAll(getServiceSpecificProperties());      
        }

        private void configurePersistence() {
            binder().install(new JpaPersistModule("argus-pu"));
        }

        private void configureLogging() {
            InputStream is = null;

            try {
                String rootName = Logger.ROOT_LOGGER_NAME;
                Logger root = (Logger) LoggerFactory.getLogger(rootName);
                LoggerContext context = root.getLoggerContext();
                JoranConfigurator configurator = new JoranConfigurator();

                is = getClass().getResourceAsStream("/META-INF/logback.xml");
                context.reset();
                configurator.setContext(context);
                configurator.doConfigure(is);
                root.setLevel(Level.ERROR);
            } catch (JoranException ex) {
                throw new SystemException(ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        assert false : "This should never occur.";
                    }
                }
            }
        }

        private void configureServices() {
            bindConcreteClass(Property.CACHE_SERVICE_IMPL_CLASS, CacheService.class);
            bindConcreteClass(Property.MQ_SERVICE_IMPL_CLASS, MQService.class);
            bindConcreteClass(Property.ALERT_SERVICE_IMPL_CLASS, AlertService.class);
            bindConcreteClass(Property.SCHEDULING_SERVICE_IMPL_CLASS, SchedulingService.class);
            bindConcreteClass(Property.MAIL_SERVICE_IMPL_CLASS, MailService.class);
            bindConcreteClass(Property.AUTH_SERVICE_IMPL_CLASS, AuthService.class);
            bindConcreteClass(Property.SCHEMA_SERVICE_IMPL_CLASS, SchemaService.class);

            // Named annotation binding
            bindConcreteClassWithNamedAnnotation(Property.TSDB_SERVICE_IMPL_CLASS, TSDBService.class);

            // static binding
            bindConcreteClass(CachedTSDBService.class, TSDBService.class);
            bindConcreteClass(DefaultUserService.class, UserService.class);
            bindConcreteClass(DefaultDashboardService.class, DashboardService.class);
            bindConcreteClass(DefaultCollectionService.class, CollectionService.class);
            bindConcreteClass(DefaultMetricService.class, MetricService.class);
            bindConcreteClass(DefaultGlobalInterlockService.class, GlobalInterlockService.class);
            bindConcreteClass(DefaultMonitorService.class, MonitorService.class);
            bindConcreteClass(DefaultWardenService.class, WardenService.class);
            bindConcreteClass(DefaultAnnotationService.class, AnnotationService.class);
            bindConcreteClass(DefaultManagementService.class, ManagementService.class);
            bindConcreteClass(DefaultServiceManagementService.class, ServiceManagementService.class);
            bindConcreteClass(DefaultAuditService.class, AuditService.class);
            bindConcreteClass(DefaultHistoryService.class, HistoryService.class);
            bindConcreteClass(DefaultNamespaceService.class, NamespaceService.class);
            bindConcreteClass(DefaultDiscoveryService.class, DiscoveryService.class);
            //TODO
            //bindConcreteClass(DefaultWaaSService.class, WaaSService.class);
        }

        private <T> void bindConcreteClass(Property property, Class<T> type) {
            bind(type).to(getConcreteClassToBind(property, type));
        }

        private <T, S> void bindConcreteClassWithNamedAnnotation(Property property, Class<T> type) {
            bind(type).annotatedWith(NamedBinding.class).to(getConcreteClassToBind(property, type));
        }

        @SuppressWarnings("unchecked")
        private <T> Class<? extends T> getConcreteClassToBind(Property property, Class<T> type) {
            try {
                return (Class<? extends T>) Class.forName(_systemConfiguration.getValue(property));
            } catch (ClassNotFoundException e) {
                assert false : "This should never occur. Failed to bind the concrete class for " + property.name();
                return null;
            }
        }

        private <I, T extends I> void bindConcreteClass(Class<T> implClassType, Class<I> interfaceType) {
            bind(interfaceType).to(implClassType);
        }

        private Properties getServiceSpecificProperties() {
            Properties properties = new Properties();

            readFile(properties, _systemConfiguration.getValue(Property.CACHE_SERVICE_PROPERTY_FILE));
            readFile(properties, _systemConfiguration.getValue(Property.MQ_SERVICE_PROPERTY_FILE));
            readFile(properties, _systemConfiguration.getValue(Property.ALERT_SERVICE_PROPERTY_FILE));
            readFile(properties, _systemConfiguration.getValue(Property.SCHEDULING_SERVICE_PROPERTY_FILE));
            readFile(properties, _systemConfiguration.getValue(Property.MAIL_SERVICE_PROPERTY_FILE));
            readFile(properties, _systemConfiguration.getValue(Property.AUTH_SERVICE_PROPERTY_FILE));
            readFile(properties, _systemConfiguration.getValue(Property.SCHEMA_SERVICE_PROPERTY_FILE));
            readFile(properties, _systemConfiguration.getValue(Property.TSDB_SERVICE_PROPERTY_FILE));
            readFile(properties, _systemConfiguration.getValue(Property.NOTIFIER_PROPERTY_FILE)); 
            return properties;
        }
    }
    public SystemMain getInstance() {
        Properties config = new Properties();
        InputStream is = null;

        try {
            is = getClass().getResourceAsStream("/argus.properties");
            config.load(is);
        } catch (IOException ex) {
            throw new SystemException(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    assert false : "This should never occur.";
                }
            }
        }
        return Guice.createInjector(new WardenTestModule(config)).getInstance(SystemMain.class);
    }  
    private void setupEmbeddedKafka() {
        Properties properties = new Properties();

        properties.put("zookeeper.connect", zkTestServer.getConnectString());
        properties.put("host.name", "localhost");
        properties.put("port", "9093");
        properties.put("broker.id", "0");
        properties.put("num.partitions", "2");
        properties.put("log.flush.interval.ms", "10");
        properties.put("log.dir", "/tmp/kafka-logs/" + createRandomName());

        KafkaConfig config = new KafkaConfig(properties);

        kafkaServer = new KafkaServerStartable(config);
        kafkaServer.startup();
    }
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
