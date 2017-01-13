package com.salesforce.dva.argus.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.beanutils.BeanUtils;
import com.salesforce.dva.argus.entity.*;
import com.salesforce.dva.warden.dto.Entity;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.dto.Resource.MetaKey;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WaaSObjectConverter implements Serializable {

    //~ Instance fields ******************************************************************************************************************************

    private BigInteger id;
    private BigInteger createdById;
    private Date createdDate;
    private BigInteger modifiedById;
    private Date modifiedDate;

    //~ Methods **************************************************************************************************************************************

    /**
     * Creates BaseDto object and copies properties from entity object.
     *
     * @param   <D>     BaseDto object type.
     * @param   <E>     Entity type.
     * @param   clazz   BaseDto entity class.
     * @param   entity  entity object.
     *
     * @return  BaseDto object.
     *
     * @throws  WebApplicationException  The exception with 500 status will be thrown.
     */
    public static <D extends Entity, E extends JPAEntity> D createDtoObject(Class<D> clazz, E entity) {
        D result = null;

        try {
            result = clazz.newInstance();
           
            try{
            	BeanUtils.copyProperties(result, entity);
            }catch (IllegalArgumentException e){
            	throw new WebApplicationException("2 exception.", Status.INTERNAL_SERVER_ERROR);
            }
            

            // Now set IDs of JPA entity
            result.setCreatedById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null);
            result.setModifiedById(entity.getModifiedBy() != null ? entity.getModifiedBy().getId() : null);
        } catch (Exception ex) {
            throw new WebApplicationException("DTO transformation failed.", Status.INTERNAL_SERVER_ERROR);
        }
        return result;
    }
    /**
     * Convert from policy entity to policy dto,
     * reset field: metricName, suspensionLevels
     * 
     * @param policy
     * @return policy dto
     */
    public static com.salesforce.dva.warden.dto.Policy convertToPolicyDto(Policy policy) {
    	if (policy == null) {
            throw new WebApplicationException("Null policy object cannot be converted to Policy Dto object.", Status.INTERNAL_SERVER_ERROR);
        }
    	//com.salesforce.dva.warden.dto.Policy result = createDtoObject(com.salesforce.dva.warden.dto.Policy.class, policy);
    	com.salesforce.dva.warden.dto.Policy result = new com.salesforce.dva.warden.dto.Policy();
    	result.setAggregator(com.salesforce.dva.warden.dto.Policy.Aggregator.valueOf(policy.getAggregator().name()));
    	result.setCreatedById(policy.getCreatedBy() != null ? policy.getCreatedBy().getId() : null);
    	result.setCreatedDate(policy.getCreatedDate());
    	result.setCronEntry(policy.getCronEntry());
    	result.setDefaultValue(policy.getDefaultValue());
    	result.setId(policy.getId());
        result.setModifiedById(policy.getModifiedBy() != null ? policy.getModifiedBy().getId() : null);
    	result.setModifiedDate(policy.getModifiedDate());
    	result.setName(policy.getName());
    	result.setOwners(policy.getOwners());
    	result.setService(policy.getService());
    	result.setSubSystem(policy.getSubSystem());
    	
    	
    	result.setSuspensionLevels(policy.getSuspensionLevels().stream().map(s -> convertToLevelDto(s)).collect(Collectors.toList()));
    	result.setThresholds(policy.getThresholds());
    	result.setTimeUnit(policy.getTimeUnit());
    	result.setTriggerType(com.salesforce.dva.warden.dto.Policy.TriggerType.valueOf(policy.getTriggerType().name()));
    	result.setUsers(policy.getUsers());
    	return result;
    }
    /**
     * Convert from policy entity list to policy dto list,
     * reset field: metricName, suspensionLevels
     * 
     * @param policy list
     * @return policy dto list
     */
    public static List<com.salesforce.dva.warden.dto.Policy> convertToPolicyDtos(List<Policy> policies){
    	if(policies == null || policies.isEmpty()){
    		throw new WebApplicationException("Null policy object cannot be converted to Policy Dto object.", Status.INTERNAL_SERVER_ERROR);
    	}
    	List<com.salesforce.dva.warden.dto.Policy>  result = new ArrayList<com.salesforce.dva.warden.dto.Policy>();
    	for(Policy p : policies){
    		result.add(convertToPolicyDto(p));
    	}
    	return result;
    }
    
    /*
     * map suspensionLevel entity to suspensionLevel dto
     */
    public static com.salesforce.dva.warden.dto.SuspensionLevel convertToLevelDto(SuspensionLevel suspensionLevel) {
    	if (suspensionLevel == null) {
            throw new WebApplicationException("Null entity object cannot be converted to Dto object.", Status.INTERNAL_SERVER_ERROR);
        }
    	//com.salesforce.dva.warden.dto.SuspensionLevel result = WaaSObjectConverter.createDtoObject(com.salesforce.dva.warden.dto.SuspensionLevel.class, suspensionLevel);
    	com.salesforce.dva.warden.dto.SuspensionLevel result = new com.salesforce.dva.warden.dto.SuspensionLevel();
    	
    	result.setPolicyId(suspensionLevel.getPolicy().getId());
        result.setLevelNumber(suspensionLevel.getLevelNumber());
        result.setInfractionCount(suspensionLevel.getInfractionCount());
        result.setSuspensionTime(BigInteger.valueOf(suspensionLevel.getSuspensionTime()));
        
        result.setCreatedById(suspensionLevel.getCreatedBy() != null ? suspensionLevel.getCreatedBy().getId() : null);
    	result.setCreatedDate(suspensionLevel.getCreatedDate());
    	result.setId(suspensionLevel.getId());
        result.setModifiedById(suspensionLevel.getModifiedBy() != null ? suspensionLevel.getModifiedBy().getId() : null);
    	result.setModifiedDate(suspensionLevel.getModifiedDate());
    	
        return result;
    }
    public static List<com.salesforce.dva.warden.dto.SuspensionLevel> convertToLevelDtos(
			List<com.salesforce.dva.argus.entity.SuspensionLevel> levels) {
    	if(levels == null || levels.isEmpty()){
    		throw new WebApplicationException("Null suspension level object cannot be converted to Policy Dto object.", Status.INTERNAL_SERVER_ERROR);
    	}
    	List<com.salesforce.dva.warden.dto.SuspensionLevel>  result = new ArrayList<com.salesforce.dva.warden.dto.SuspensionLevel>();
    	for(SuspensionLevel l : levels){
    		result.add(convertToLevelDto(l));
    	}
    	return result;
	}
    /*
     * map infraction entity to infraction dto
     */
    public static com.salesforce.dva.warden.dto.Infraction convertToInfractionDto(Infraction infraction) {
    	if (infraction == null) {
            throw new WebApplicationException("Null entity object cannot be converted to Dto object.", Status.INTERNAL_SERVER_ERROR);
        }
    	com.salesforce.dva.warden.dto.Infraction result = new com.salesforce.dva.warden.dto.Infraction();
    	
    	result.setPolicyId(infraction.getPolicy().getId());
    	result.setUserId(infraction.getUser().getId());
    	result.setInfractionTimestamp(infraction.getInfractionTimestamp());
    	result.setExpirationTimestamp(infraction.getExpirationTimestamp());
    	
    	result.setCreatedById(infraction.getCreatedBy() != null ? infraction.getCreatedBy().getId() : null);
    	result.setCreatedDate(infraction.getCreatedDate());
    	result.setId(infraction.getId());
        result.setModifiedById(infraction.getModifiedBy() != null ? infraction.getModifiedBy().getId() : null);
    	result.setModifiedDate(infraction.getModifiedDate());
        
        return result;
    }
    public static List<com.salesforce.dva.warden.dto.Infraction> convertToInfractionDtos(
			List<com.salesforce.dva.argus.entity.Infraction> infractions) {
    	if(infractions == null || infractions.isEmpty()){
    		throw new WebApplicationException("Null infraction object cannot be converted to infraction Dto object.", Status.INTERNAL_SERVER_ERROR);
    	}
    	List<com.salesforce.dva.warden.dto.Infraction>  result = new ArrayList<com.salesforce.dva.warden.dto.Infraction>();
    	for(Infraction i : infractions){
    		result.add(convertToInfractionDto(i));
    	}
    	return result;
	}
    
    /*
     * map infraction entity to infraction dto
     */
    public static com.salesforce.dva.warden.dto.User convertToWardenUserDto(PrincipalUser principalUser) {
    	if (principalUser == null) {
            throw new WebApplicationException("Null entity object cannot be converted to Dto object.", Status.INTERNAL_SERVER_ERROR);
        }
    	com.salesforce.dva.warden.dto.User result = new com.salesforce.dva.warden.dto.User();
    	result.setUsername(principalUser.getUsername());
    	result.setEmail(principalUser.getEmail());
    	
    	result.setCreatedById(principalUser.getCreatedBy() != null ? principalUser.getCreatedBy().getId() : null);
    	result.setCreatedDate(principalUser.getCreatedDate());
    	result.setId(principalUser.getId());
        result.setModifiedById(principalUser.getModifiedBy() != null ? principalUser.getModifiedBy().getId() : null);
    	result.setModifiedDate(principalUser.getModifiedDate());
    	
    	return result;
    }
    public static List<com.salesforce.dva.warden.dto.User> convertToWardenUserDtos(
			List<com.salesforce.dva.argus.entity.PrincipalUser> principalUsers) {
    	if(principalUsers == null || principalUsers.isEmpty()){
    		throw new WebApplicationException("Null principal Users object cannot be converted to infraction Dto object.", Status.INTERNAL_SERVER_ERROR);
    	}
    	List<com.salesforce.dva.warden.dto.User>  result = new ArrayList<com.salesforce.dva.warden.dto.User>();
    	for(PrincipalUser p : principalUsers){
    		result.add(convertToWardenUserDto(p));
    	}
    	return result;
	}
    

 public static Map<Long, Double> convertToMetricDtos(
			List<com.salesforce.dva.argus.entity.Metric> metrics) {
 	if(metrics == null || metrics.isEmpty()){
 		throw new WebApplicationException("Null metrics object cannot be converted to infraction Dto object.", Status.INTERNAL_SERVER_ERROR);
 	}
 	Map<Long, Double>  result = new TreeMap<>();
 	for(Metric m : metrics){
 		         for (Map.Entry<Long,String> datapoint : m.getDatapoints().entrySet()) {
                result.put(datapoint.getKey(), Double.valueOf(datapoint.getValue()));
            }
 	}
 	return result;
	}

 public static com.salesforce.dva.warden.dto.Subscription convertToSubscriptionDto(Class<com.salesforce.dva.warden.dto.Subscription> clazz,com.salesforce.dva.argus.entity.Subscription subscriptionEntity) {
	 if (subscriptionEntity == null) {
         throw new WebApplicationException("Null entity object cannot be converted to Dto object.", Status.INTERNAL_SERVER_ERROR);
     }
	 com.salesforce.dva.warden.dto.Subscription result = null;

     try {
         result = clazz.newInstance();
         BeanUtils.copyProperties(result, subscriptionEntity);

     } catch (Exception ex) {
         throw new WebApplicationException("Subscription DTO transformation failed.", Status.INTERNAL_SERVER_ERROR);
     }
     return result;
	}
    //~ Methods **************************************************************************************************************************************

   
	/**
     * Returns the entity ID.
     *
     * @return  The entity ID.
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Specifies the entity ID.
     *
     * @param  id  The entity ID.
     */
    public void setId(BigInteger id) {
        this.id = id;
    }

    /**
     * Returns the created date.
     *
     * @return  The created date.
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Specifies the created date.
     *
     * @param  createdDate  The created date.
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Returns the ID of the creator.
     *
     * @return  The ID of the creator.
     */
    public BigInteger getCreatedById() {
        return createdById;
    }

    /**
     * Specifies the ID of the creator.
     *
     * @param  createdById  The ID of the creator.
     */
    public void setCreatedById(BigInteger createdById) {
        this.createdById = createdById;
    }

    /**
     * Returns the ID of the last person who modified the entity.
     *
     * @return  The ID of the last person who modified the entity.
     */
    public BigInteger getModifiedById() {
        return modifiedById;
    }

    /**
     * Specifies the ID of the person who most recently modified the entity.
     *
     * @param  modifiedById  The ID of the person who most recently modified the entity.
     */
    public void setModifiedById(BigInteger modifiedById) {
        this.modifiedById = modifiedById;
    }

    /**
     * Returns the modified on date.
     *
     * @return  The modified on date.
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * Specifies the modified on date.
     *
     * @param  modifiedDate  The modified on date.
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
	public static <T> Resource<T> convertToResource(T entity, EnumMap<MetaKey, String> meta) {
		
		Resource<T> resource = new Resource<T>();
		resource.setEntity(entity);
		resource.setMeta(meta);
		
		return resource;
	}
	
	
}
