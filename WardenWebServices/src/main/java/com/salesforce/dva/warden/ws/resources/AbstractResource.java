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
package com.salesforce.dva.warden.ws.resources;

import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.argus.service.AuthService;
import com.salesforce.dva.argus.service.ManagementService;
import com.salesforce.dva.argus.service.UserService;
import com.salesforce.dva.argus.service.WaaSService;
import com.salesforce.dva.argus.system.SystemMain;
import com.salesforce.dva.warden.ws.dto.EndpointHelp;
import com.salesforce.dva.warden.ws.dto.MethodHelp;
import com.salesforce.dva.warden.ws.filter.AuthFilter;
import com.salesforce.dva.warden.ws.listeners.WebServletListener;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Metric;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.dto.Subscription;
import com.salesforce.dva.warden.dto.SuspensionLevel;
import com.salesforce.dva.warden.dto.User;
import com.salesforce.dva.warden.ws.dto.Converter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.TreeMap;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Abstract base class for web service resource.
 *
 * <p>
 * Subclasses should implement the help() method.</p>
 *
 * @author Raj Sarkapally (rsarkapally@salesforce.com)
 */
public abstract class AbstractResource {

    @Context
    protected UriInfo uriInfo;
    @Context 
    protected ResourceContext rc;
    

    /**
     * Used to provide description information about web service endpoints and methods.
     *
     * @author Tom Valine (tvaline@salesforce.com)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.METHOD, ElementType.TYPE})
    public static @interface Description {

        public String value();
    }

    //~ Instance fields ******************************************************************************************************************************
    protected final SystemMain system = WebServletListener.getSystem();
    protected final UserService userService = system.getServiceFactory().getUserService();
    protected final WaaSService waaSService = system.getServiceFactory().getWaaSService();
    protected final AuthService authService = system.getServiceFactory().getAuthService();
    protected final ManagementService managementService = system.getServiceFactory().getManagementService();



    //~ Methods **************************************************************************************************************************************
    /**
     * Generates a list of endpoint help DTOs used to describe the major service endpoints.
     *
     * @param resourceClasses The resource classes to describe.
     *
     * @return The list of endpoint help DTOs.
     */
    protected static List<EndpointHelp> describeEndpoints(List<Class<? extends AbstractResource>> resourceClasses) {
        List<EndpointHelp> result = new LinkedList<>();

        if (resourceClasses != null && !resourceClasses.isEmpty()) {
            for (Class<? extends AbstractResource> resourceClass : resourceClasses) {
                EndpointHelp dto = EndpointHelp.fromResourceClass(resourceClass);

                if (dto != null) {
                    result.add(dto);
                }
            }
        }
        return result;
    }

    //~ Methods **************************************************************************************************************************************
    /**
     * Returns the logged in user object.
     *
     * @param req The HTTP request.
     *
     * @return The logged in user object.
     */
    protected PrincipalUser getRemoteUser(HttpServletRequest req) {
        requireArgument(req != null, "Request cannot be null.");

        PrincipalUser result = null;
        Object principalAttribute = req.getSession(true).getAttribute(AuthFilter.USER_ATTRIBUTE_NAME);

        if (principalAttribute != null) {
            User user = User.class.cast(principalAttribute);

            result = userService.findUserByUsername(user.getUsername());
        }
        return result;
    }

    /**
     * Throws an illegal argument exception if the condition is not met.
     *
     * @param condition The boolean condition to check.
     * @param message The exception message.
     *
     * @throws WebApplicationException If the condition is not met.
     */
    protected static void requireThat(boolean condition, String message) {
        if (!condition) {
            throw new WebApplicationException(message, Status.BAD_REQUEST);
        }
    }

    /**
     * Throws an illegal argument exception if the condition is not met.
     *
     * @param condition The boolean condition to check.
     * @param message The exception message.
     * @param status The status to reflect.
     *
     * @throws WebApplicationException If the condition is not met.
     */
    protected static void requireThat(boolean condition, String message, Status status) {
        if (!condition) {
            throw new WebApplicationException(message, status);
        }
    }

    /**
     * Returns the help for the endpoint. For the context root, it will return the endpoint help for all major endpoints. For a specific endpoint it
     * will return the method help for the endpoint.
     *
     * @return Help object describing the service in JSON format.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/help")
    public Map<String, List<? extends Object>> help() {
        Map<String, List<?>> result = new LinkedHashMap<>();
        List<EndpointHelp> endpoints = describeEndpoints(getEndpoints());

        if (endpoints != null && !endpoints.isEmpty()) {
            result.put("endpoints", endpoints);
        }

        List<MethodHelp> methods = describeMethods();

        if (methods != null && !methods.isEmpty()) {
            result.put("methods", methods);
        }
        return result;
    }

    /**
     * Overridden by the context root to describe the available endpoints. Specific service endpoints should always return null as they will only make
     * available the method help.
     *
     * @return The list of endpoints for which to make help information available.
     */
    public List<Class<? extends AbstractResource>> getEndpoints() {
        return null;
    }

    /**
     * Generates help DTOs for each method on the service interface. The root context shall return a null list. All other endpoints will re-use this
     * implementation.
     *
     * @return The service endpoint method help objects.
     */
    protected List<MethodHelp> describeMethods() {
        List<MethodHelp> result = new LinkedList<>();
        Path endpointPath = getClass().getAnnotation(Path.class);

        for (Method method : getClass().getDeclaredMethods()) {
            String parentPath = endpointPath == null ? null : endpointPath.value();
            MethodHelp methodHelpDto = MethodHelp.fromMethodClass(parentPath, method);

            if (methodHelpDto != null) {
                result.add(methodHelpDto);
            }
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Validates the owner name and returns the owner object.
     *
     * @param req The HTTP request.
     * @param ownerName Name of the owner. It is optional.
     *
     * @return The owner object
     *
     * @throws WebApplicationException Throws exception if owner name does not exist.
     */
    protected PrincipalUser validateAndGetOwner(HttpServletRequest req, String ownerName) {
        PrincipalUser remoteUser = getRemoteUser(req);

        if (ownerName == null || ownerName.isEmpty()) {
            return remoteUser;
        } else if (ownerName.equalsIgnoreCase(remoteUser.getUserName())) {
            return remoteUser;
        } else if (remoteUser.isPrivileged()) {
            PrincipalUser owner;

            owner = userService.findUserByUsername(ownerName);
            if (owner == null) {
                throw new WebApplicationException(ownerName + ": User does not exist.", Status.NOT_FOUND);
            } else {
                return owner;
            }
        }
        throw new WebApplicationException(Status.FORBIDDEN.getReasonPhrase(), Status.FORBIDDEN);
    }

    /**
     * Validates the resource authorization. Throws exception if the user is not authorized to access the resource.
     *
     * @param req The HTTP request.
     * @param actualOwner The owner of the resource.
     * @param currentOwner The logged in user.
     *
     * @throws WebApplicationException Throws exception if user is not authorized to access the resource.
     */
    protected void validateResourceAuthorization(HttpServletRequest req, PrincipalUser actualOwner, PrincipalUser currentOwner) {
        if (!getRemoteUser(req).isPrivileged() && !actualOwner.equals(currentOwner)) {
            throw new WebApplicationException(Status.FORBIDDEN.getReasonPhrase(), Status.FORBIDDEN);
        }
    }

    /**
     * Validates that the user making the request is a privileged user.
     *
     * @param req - Http Request
     *
     * @throws WebApplicationException Throws exception if user is not a privileged user.
     */
    protected void validatePrivilegedUser(HttpServletRequest req) {
        if (!getRemoteUser(req).isPrivileged()) {
            throw new WebApplicationException(Status.FORBIDDEN.getReasonPhrase(), Status.FORBIDDEN);
        }
    }

    /**
     * Copies properties.
     *
     * @param dest The object to which the properties will be copied.
     * @param source The object whose properties are copied
     *
     * @throws WebApplicationException Throws exception if beanutils encounter a problem.
     */
    protected void copyProperties(Object dest, Object source) {
        try {
            BeanUtils.copyProperties(dest, source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new WebApplicationException(e.toString(), Status.BAD_REQUEST);
        }
    }

    protected EnumMap<Resource.MetaKey, String> createMetadata(URI userUri, int statusCode, String method, String message, String uiMessage, String devMessage) {
        EnumMap<Resource.MetaKey, String> meta = new EnumMap<>(Resource.MetaKey.class);
        meta.put(Resource.MetaKey.href, userUri.toString());
        meta.put(Resource.MetaKey.status, Integer.toString(statusCode));
        meta.put(Resource.MetaKey.verb, method);
        meta.put(Resource.MetaKey.message, message);
        meta.put(Resource.MetaKey.uiMessage, uiMessage);
        meta.put(Resource.MetaKey.devMessage, devMessage);
        return meta;
    }

    Policy fromEntity(com.salesforce.dva.argus.entity.Policy policy) {
        Policy result = Converter.fromEntity(Policy.class, policy);
        result.setAggregator(policy.getAggregator());
        result.setTriggerType(policy.getTriggerType());
        List<BigInteger> suspensionLevelIds = new ArrayList<>();
        policy.getSuspensionLevels().stream().forEach(l -> suspensionLevelIds.add(l.getId()));
        result.setSuspensionLevels(suspensionLevelIds);
        return result;
    }

    Infraction fromEntity(com.salesforce.dva.argus.entity.Infraction infraction) {
        Infraction result = Converter.fromEntity(Infraction.class, infraction);
        result.setUserId(infraction.getUser().getId());
        result.setUsername(infraction.getUser().getUserName());
        result.setPolicyId(infraction.getPolicy().getId());
        return result;
    }

    SuspensionLevel fromEntity(com.salesforce.dva.argus.entity.SuspensionLevel level) {
        SuspensionLevel result = Converter.fromEntity(SuspensionLevel.class, level);
        result.setPolicyId(level.getPolicy().getId());
        return result;
    }

    com.salesforce.dva.argus.entity.Infraction toEntity(Infraction infraction) {
        com.salesforce.dva.argus.entity.Infraction result = Converter.toEntity(com.salesforce.dva.argus.entity.Infraction.class, infraction);
        PrincipalUser creator = infraction.getCreatedById() == null ? null : userService.findUserByPrimaryKey(infraction.getCreatedById());
        PrincipalUser modifier = infraction.getModifiedById() == null ? null : userService.findUserByPrimaryKey(infraction.getModifiedById());
        PrincipalUser user = infraction.getUserId() == null ? null : userService.findUserByPrimaryKey(infraction.getUserId());
        com.salesforce.dva.argus.entity.Policy policy = infraction.getPolicyId() == null ? null : waaSService.getPolicy(infraction.getId());
        result.setCreatedBy(creator);
        result.setModifiedBy(modifier);
        result.setUser(user);
        result.setPolicy(policy);
        return result;
    }

    com.salesforce.dva.argus.entity.Policy toEntity(Policy policy) {
        com.salesforce.dva.argus.entity.Policy result = Converter.toEntity(com.salesforce.dva.argus.entity.Policy.class, policy);
        PrincipalUser creator = policy.getCreatedById() == null ? null : userService.findUserByPrimaryKey(policy.getCreatedById());
        PrincipalUser modifier = policy.getModifiedById() == null ? null : userService.findUserByPrimaryKey(policy.getModifiedById());
        result.setModifiedBy(modifier);
        result.setCreatedBy(creator);
        List<com.salesforce.dva.argus.entity.SuspensionLevel> levels = new ArrayList<>();
        policy.getSuspensionLevels().stream().forEach(l -> levels.add(waaSService.getLevel(result, l)));
        result.setSuspensionLevels(levels);
        return result;
    }

    com.salesforce.dva.argus.entity.SuspensionLevel toEntity(SuspensionLevel level) {
        com.salesforce.dva.argus.entity.SuspensionLevel result = Converter.toEntity(com.salesforce.dva.argus.entity.SuspensionLevel.class, level);
        PrincipalUser creator = level.getCreatedById() == null ? null : userService.findUserByPrimaryKey(level.getCreatedById());
        PrincipalUser modifier = level.getModifiedById() == null ? null : userService.findUserByPrimaryKey(level.getModifiedById());
        com.salesforce.dva.argus.entity.Policy policy = level.getPolicyId() == null ? null : waaSService.getPolicy(level.getId());
        result.setModifiedBy(modifier);
        result.setCreatedBy(creator);
        result.setPolicy(policy);
        return result;
    }
    /**
     * Converts a user entity to a user transfer object.
     *
     * @param   user  The user entity. Cannot be null.
     *
     * @return  The user transfer object.
     */
     User fromEntity(PrincipalUser user) {
        User result = Converter.fromEntity(User.class, user);

        result.setEmail(user.getEmail());
        result.setUsername(user.getUserName());
        return result;
    }

    /**
     * Converts a metric entity to a metric transfer object.
     *
     * @param   metric    The metric entity. Cannot be null.
     * @param   username  The username. Cannot be null.
     * @param   policyId  The policy ID. Cannot be null.
     *
     * @return  The metric transfer object.
     */
    Metric fromEntity(com.salesforce.dva.argus.entity.Metric metric, String username, BigInteger policyId) {
        Metric result = new Metric();

        result.setPolicyId(policyId);
        result.setUsername(username);

        Map<Long, Double> datapoints = new TreeMap<>();

        for (Map.Entry<Long, String> entry : metric.getDatapoints().entrySet()) {
            datapoints.put(entry.getKey(), Double.valueOf(entry.getValue()));
        }
        result.setDatapoints(datapoints);
        return result;
    }
    
    Subscription fromEntity(com.salesforce.dva.argus.entity.Subscription subscription) {
        Subscription result = Converter.fromEntity(Subscription.class, subscription);
        return result;
    }



    //~ Enums ****************************************************************************************************************************************
    /**
     * Enumerates the supported HTTP methods.
     *
     * @author Tom Valine (tvaline@salesforce.com)
     */
    public enum HttpMethod {

        GET,
        POST,
        PUT,
        DELETE,
        HEAD,
        OPTIONS;
    }
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
