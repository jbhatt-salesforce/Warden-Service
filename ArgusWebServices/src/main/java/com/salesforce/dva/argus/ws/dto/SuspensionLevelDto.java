package com.salesforce.dva.argus.ws.dto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.salesforce.dva.argus.entity.SuspensionLevel;
/**
 * SuspensionLevel Dto.
 *
 * @author  Ruofan Zhang (rzhang@salesforce.com)
 */
@SuppressWarnings("serial")
//@JsonIgnoreProperties(ignoreUnknown = true)
public class SuspensionLevelDto extends EntityDTO {
	//~ Instance fields ******************************************************************************************************************************
    
    private BigInteger policyId;    
    private int levelNumber;   
    private int infractionCount;    
    private long suspensionTime;
    
  //~ Methods **************************************************************************************************************************************

    /**
     * Converts SuspensionLevel entity to SuspensionLevelDto.
     *
     * @param   SuspensionLevel  The SuspensionLevel object. Cannot be null.
     *
     * @return  SuspensionLevelDto object.
     *
     * @throws  WebApplicationException  If an error occurs.
     */
    public static SuspensionLevelDto transformToDto(SuspensionLevel level) {
        if (level == null) {
            throw new WebApplicationException("Null entity object cannot be converted to Dto object.", Status.INTERNAL_SERVER_ERROR);
        }

        SuspensionLevelDto result = createDtoObject(SuspensionLevelDto.class, level);  
        
        return result;
    }

    /**
     * Converts list of SuspensionLevel entity objects to list of SuspensionLevelDto objects.
     *
     * @param   SuspensionLevel  List of Policy entities. Cannot be null.
     *
     * @return  List of SuspensionLevelDto objects.
     *
     * @throws  WebApplicationException  If an error occurs.
     */
    public static List<SuspensionLevelDto> transformToDto(List<SuspensionLevel> levels) {
        if (levels == null) {
            throw new WebApplicationException("Null entity object cannot be converted to Dto object.", Status.INTERNAL_SERVER_ERROR);
        }

        List<SuspensionLevelDto> result = new ArrayList<SuspensionLevelDto>();

        for (SuspensionLevel level : levels) {
            result.add(transformToDto(level));
        }
        return result;
    }

	public BigInteger getPolicyId() {
		return policyId;
	}

	public void setPolicyId(BigInteger policyId) {
		this.policyId = policyId;
	}

	public int getLevelNumber() {
		return levelNumber;
	}

	public void setLevelNumber(int levelNumber) {
		this.levelNumber = levelNumber;
	}

	public int getInfractionCount() {
		return infractionCount;
	}

	public void setInfractionCount(int infractionCount) {
		this.infractionCount = infractionCount;
	}

	public long getSuspensionTime() {
		return suspensionTime;
	}

	public void setSuspensionTime(long suspensionTime) {
		this.suspensionTime = suspensionTime;
	}

	@Override
	public Object createExample() {
		SuspensionLevelDto result = new SuspensionLevelDto();		
		
		result.setId(BigInteger.ONE);
		result.setCreatedById(BigInteger.ONE);
		result.setCreatedDate(new Date());
		result.setModifiedById(BigInteger.TEN);
		result.setModifiedDate(new Date());
		
		result.setPolicyId(BigInteger.ONE);
		result.setLevelNumber(1);
		result.setInfractionCount(10);
		result.setSuspensionTime(Long.MAX_VALUE);
		return result;
	}    
}
