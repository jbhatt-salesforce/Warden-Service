package com.salesforce.dva.warden.ws.dto;

import com.salesforce.dva.argus.entity.JPAEntity;
import com.salesforce.dva.warden.dto.Entity;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.beanutils.BeanUtils;

public class Converter {
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
    public static <D extends Entity, E extends JPAEntity> D fromEntity(Class<D> clazz, E entity) {
        D result = null;

        try {
            result = clazz.newInstance();
            BeanUtils.copyProperties(result, entity);

            result.setCreatedById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null);
            result.setModifiedById(entity.getModifiedBy() != null ? entity.getModifiedBy().getId() : null);
        } catch (Exception ex) {
            throw new WebApplicationException("DTO transformation failed.", Status.INTERNAL_SERVER_ERROR);
        }
        return result;
    }
}
