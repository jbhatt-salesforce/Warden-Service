package com.salesforce.dva.argus.service;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.salesforce.dva.argus.AbstractTest;
import com.salesforce.dva.argus.IntegrationTest;
import com.salesforce.dva.argus.entity.Alert;
import com.salesforce.dva.argus.entity.Annotation;
import com.salesforce.dva.argus.entity.Notification;
import com.salesforce.dva.argus.entity.Policy;
import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.argus.entity.Trigger;
import com.salesforce.dva.argus.entity.Trigger.TriggerType;
import com.salesforce.dva.argus.service.AlertService.Notifier;
import com.salesforce.dva.argus.service.AlertService.SupportedNotifier;
import com.salesforce.dva.argus.service.alert.DefaultAlertService.NotificationContext;
//import com.salesforce.dva.warden.dto.Policy.Aggregator;
//import com.salesforce.dva.warden.dto.Policy.TriggerType;

@Category(IntegrationTest.class)
public class WaaSNotifierIT extends AbstractTest {

    

    private static final String waasExpression = 
    		"-1h:service.subsystem:policyName.descriptor.max{user=rzhang}:avg";
    
    @Inject

    private UserService _userService;
    private WaaSService _waaSService;
    private AlertService _alertService;
    private static EntityManagerFactory factory;
	private static EntityManager em;
    
	private static Policy testPolicy = null;
	private static final String NAME = "policyName.descriptor.max";
    private static final String SERVICE = "service";
    private static final com.salesforce.dva.warden.dto.Policy.TriggerType TRIGGER_TYPE = com.salesforce.dva.warden.dto.Policy.TriggerType.EQUAL;
    private static final com.salesforce.dva.warden.dto.Policy.Aggregator AGGREGATOR = com.salesforce.dva.warden.dto.Policy.Aggregator.AVG;
    private static final String TIME_UNIT = "5m";
    private static final String CRON_ENTRY = "* * * * *";
    private static final double DEFAULT_VALUE = 99.0;
    private static final List<Double> THRESHOLD = Arrays.asList(100.0);
    
    private static final int LEVEL_NUMBER = 3;
    private static final int INFRACTION_COUNT = 3;
    private static final long SUSPENSION_TIME = 3 * 1000L;
    private static final String POLICY = "policy";
    private static final String WAAS_ALERT_NAME_PREFIX = "waas-";
    
    
   
    @Rule public TestName testName = new TestName();
    PrincipalUser user = null;
    @SuppressWarnings("static-access")
	@Before
    @Override
    public void setUp() {
        super.setUp();
        _userService = system.getServiceFactory().getUserService();
        _waaSService = system.getServiceFactory().getWaaSService();
        _alertService = system.getServiceFactory().getAlertService();
        
        factory = Persistence.createEntityManagerFactory("argus-pu");
		em = factory.createEntityManager();
		
        user = _userService.findUserByUsername("rzhang");
        
        if (user == null) {
            user = new PrincipalUser("rzhang", "rzhang@salesforce.com");
            user = _userService.updateUser(user);
        }
        
        /* create policy and suspensionLevel for testing */
        String username = user.getUsername();		
		testPolicy = new Policy(user, this.SERVICE, this.NAME, Arrays.asList(NAME),
				Arrays.asList(username), this.TRIGGER_TYPE, this.AGGREGATOR, this.THRESHOLD, this.TIME_UNIT,
				this.DEFAULT_VALUE, this.CRON_ENTRY);
		testPolicy.setSubSystem("subsystem");
		
		//testPolicy.setName();

		  
    }

    @After
    @Override
    public void tearDown() {
    	try{
    		em.getTransaction().begin();
        	em.flush();
        	em.getTransaction().commit();
        	em.close();
        	factory.close();
        	super.tearDown();
    	}catch (Exception ex) {
    		throw new RuntimeException("failed to tear down!!!");
    	}
    }
    
    @Test
    public void testWaaSNotifier() throws InterruptedException {
    	Policy oldPolicy = _waaSService.updatePolicy(testPolicy);
        UserService userService = system.getServiceFactory().getUserService();
        PrincipalUser user = new PrincipalUser("aUser", "aUser@salesforce.com");

        user.setCreatedBy(user);
        user = userService.updateUser(user);

        Alert alert = new Alert(userService.findAdminUser(), user, "waas-" + user.getUsername() + "-service.subsystem:policyName.descriptor.max", waasExpression, "* * * * *");
       
        Notification notification = new Notification("notification_name", alert, "notifier_name", new ArrayList<String>(), 23);
        Trigger trigger = new Trigger(alert, TriggerType.GREATER_THAN_OR_EQ, "trigger_name", 2D, 5);

        alert.setNotifications(Arrays.asList(new Notification[] { notification }));
        alert.setTriggers(Arrays.asList(new Trigger[] { trigger }));

        alert = system.getServiceFactory().getAlertService().updateAlert(alert);

        NotificationContext context = new NotificationContext(alert, trigger, notification, System.currentTimeMillis(), "foo","bar");
        Notifier notifier = system.getServiceFactory().getAlertService().getNotifier(SupportedNotifier.WAAS);

        notifier.sendNotification(context);
        Thread.sleep(2000);

        List<Annotation> annotations = system.getServiceFactory().getAnnotationService().getAnnotations(        		
        		"-3s:service.subsystem:policyName.descriptor.max:WAAS:aUser");
        assertFalse(annotations.isEmpty());

        Annotation annotation = annotations.get(annotations.size() - 1);

        if (System.currentTimeMillis() - annotation.getTimestamp() < 10000) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }
    
    
   

}

