package com.salesforce.dva.argus.ws.dto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.salesforce.dva.argus.entity.Policy;
import com.salesforce.dva.argus.entity.SuspensionLevel;
import com.salesforce.dva.argus.entity.Trigger.TriggerType;

/**
 * Policy Dto.
 *
 * @author  Ruofan Zhang (rzhang@salesforce.com)
 */

@SuppressWarnings("serial")
//@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyDto extends EntityDTO {
	//~ Instance fields ******************************************************************************************************************************

	    private String servcie;		
	    private String name;
	    private List<String> owner  = new ArrayList<String>();
	    private List<String> user  = new ArrayList<String>();
	    private String subSystem;
	    private String metricName;
	    private TriggerType triggerType;
	    private String aggregator;
	    private List<Double> threshold;	    
	    private String timeUnit;
	    private double defaultValue;
	    private String cronEntry;
	    private List<BigInteger> suspensionLevelIdList = new ArrayList<BigInteger>();
	    
	    //~ Methods **************************************************************************************************************************************

	    /**
	     * Converts Policy entity to PolicyDto.
	     *
	     * @param   policy  The alert object. Cannot be null.
	     *
	     * @return  PolicyDto object.
	     *
	     * @throws  WebApplicationException  If an error occurs.
	     */
	    public static PolicyDto transformToDto(Policy policy) {
	        if (policy == null) {
	            throw new WebApplicationException("Null entity object cannot be converted to Dto object.", Status.INTERNAL_SERVER_ERROR);
	        }

	        PolicyDto result = createDtoObject(PolicyDto.class, policy);

	     
	        for (SuspensionLevel suspensionLevel : policy.getSuspensionLevelList()) {
	            result.addSuspensionLevelIdList(suspensionLevel);;
	        }
	        
	        return result;
	    }

	    /**
	     * Converts list of Policy entity objects to list of PolicyDto objects.
	     *
	     * @param   Policy  List of Policy entities. Cannot be null.
	     *
	     * @return  List of PolicyDto objects.
	     *
	     * @throws  WebApplicationException  If an error occurs.
	     */
	    public static List<PolicyDto> transformToDto(List<Policy> policies) {
	        if (policies == null) {
	            throw new WebApplicationException("Null entity object cannot be converted to Dto object.", Status.INTERNAL_SERVER_ERROR);
	        }

	        List<PolicyDto> result = new ArrayList<PolicyDto>();

	        for (Policy policy : policies) {
	            result.add(transformToDto(policy));
	        }
	        return result;
	    }

		public String getServcie() {
			return servcie;
		}

		public void setServcie(String servcie) {
			this.servcie = servcie;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getOwner() {
			return owner;
		}

		public void setOwner(List<String> owner) {
			this.owner = owner;
		}

		public List<String> getUser() {
			return user;
		}

		public void setUser(List<String> user) {
			this.user = user;
		}

		public String getSubSystem() {
			return subSystem;
		}

		public void setSubSystem(String subSystem) {
			this.subSystem = subSystem;
		}

		public String getMetricName() {
			return metricName;
		}

		public void setMetricName(String metricName) {
			this.metricName = metricName;
		}

		public TriggerType getTriggerType() {
			return triggerType;
		}

		public void setTriggerType(TriggerType triggerType) {
			this.triggerType = triggerType;
		}

		public String getAggregator() {
			return aggregator;
		}

		public void setAggregator(String aggregator) {
			this.aggregator = aggregator;
		}

		public List<Double> getThreshold() {
			return threshold;
		}

		public void setThreshold(List<Double> threshold) {
			this.threshold = threshold;
		}

		public String getTimeUnit() {
			return timeUnit;
		}

		public void setTimeUnit(String timeUnit) {
			this.timeUnit = timeUnit;
		}

		public double getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(double defaultValue) {
			this.defaultValue = defaultValue;
		}

		public String getCronEntry() {
			return cronEntry;
		}

		public void setCronEntry(String cronEntry) {
			this.cronEntry = cronEntry;
		}

		public List<BigInteger> getSuspensionLevelIdList() {
			return suspensionLevelIdList;
		}

		public void addSuspensionLevelIdList(SuspensionLevel suspensionLevel) {
			this.getSuspensionLevelIdList().add(suspensionLevel.getId());
		}
		
		@Override
		public Object createExample() {
			PolicyDto result = new PolicyDto();
			
			result.setId(BigInteger.ONE);
			result.setCreatedById(BigInteger.ONE);
			result.setCreatedDate(new Date());
			result.setModifiedById(BigInteger.TEN);
			result.setModifiedDate(new Date());
			
			result.setServcie("example-service");
			result.setName("example-name");
			result.setOwner(Arrays.asList("example-owner"));
			result.setUser(Arrays.asList("example-user"));
			result.setSubSystem("example-subSystem");
			result.setMetricName("example-metricName");
			result.setTriggerType(TriggerType.NOT_BETWEEN);
			result.setAggregator("sum");
			result.setThreshold(Arrays.asList(0.0));
			result.setTimeUnit("5min");
			result.setDefaultValue(0.0);
			result.setCronEntry("0 */4 * * *");
			
			return result;
		}
}
