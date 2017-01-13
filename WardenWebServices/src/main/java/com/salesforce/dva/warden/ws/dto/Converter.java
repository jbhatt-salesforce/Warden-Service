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

package com.salesforce.dva.warden.ws.dto;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.beanutils.BeanUtils;
import com.salesforce.dva.argus.entity.JPAEntity;
import com.salesforce.dva.warden.dto.Entity;

/**
 * Converts between DTO and entity objects.
 */
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
            result.setCreatedById((entity.getCreatedBy() != null) ? entity.getCreatedBy().getId() : null);
            result.setModifiedById((entity.getModifiedBy() != null) ? entity.getModifiedBy().getId() : null);
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

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



