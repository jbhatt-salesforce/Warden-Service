package com.salesforce.dva.warden.ws.dto;

import com.salesforce.dva.argus.entity.JPAEntity;
import com.salesforce.dva.warden.dto.Entity;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.beanutils.BeanUtils;

public class Converter {
    /**
     * Converts from Entity to DTO.
     * @param <E> The entity type.
     * @param <D> The DTO type.
     * @param clazz The entity class.
     * @param entity The DTO instance.
     * 
     * @return The converted DTO.
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

    /**
     * Converts from DTO to Entity.
     * @param <E> The entity type.
     * @param <D> The DTO type.
     * @param clazz The entity class.
     * @param dto The DTO instance.
     * 
     * @return The converted entity.
     */
    public static <E extends JPAEntity, D extends Entity> E toEntity(Class<E> clazz, D dto) {
        E result = null;

        try {
            result = clazz.newInstance();
            BeanUtils.copyProperties(result, dto);
        } catch (Exception ex) {
            throw new WebApplicationException("DTO transformation failed.", Status.INTERNAL_SERVER_ERROR);
        }
        return result;
    }
}
