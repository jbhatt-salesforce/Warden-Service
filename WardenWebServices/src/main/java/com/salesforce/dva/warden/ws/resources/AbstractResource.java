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

package com.salesforce.dva.warden.ws.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.argus.service.AuthService;
import com.salesforce.dva.argus.service.ManagementService;
import com.salesforce.dva.argus.service.UserService;
import com.salesforce.dva.argus.service.WaaSService;
import com.salesforce.dva.argus.system.SystemMain;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Metric;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Resource.MetaKey;
import com.salesforce.dva.warden.dto.SuspensionLevel;
import com.salesforce.dva.warden.dto.User;
import com.salesforce.dva.warden.ws.dto.Converter;
import com.salesforce.dva.warden.ws.dto.EndpointHelp;
import com.salesforce.dva.warden.ws.dto.MethodHelp;
import com.salesforce.dva.warden.ws.filter.AuthFilter;
import com.salesforce.dva.warden.ws.listeners.WebServletListener;
import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

/**
 * Abstract base class for web service resource.
 *
 * Subclasses should implement the help() method.
 *
 * @author Raj Sarkapally (rsarkapally@salesforce.com)
 */
public abstract class AbstractResource {

    protected final SystemMain system = WebServletListener.getSystem();
    protected final UserService userService = system.getServiceFactory().getUserService();
    protected final WaaSService waaSService = system.getServiceFactory().getWaaSService();
    protected final AuthService authService = system.getServiceFactory().getAuthService();
    protected final ManagementService managementService = system.getServiceFactory().getManagementService();
    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext rc;

    /**
     * Helper method to create meta data for response resources.
     *
     *
     * @param uri The URI for the resource. Cannot be null.
     * @param status The response status code for the requested resource. Cannot be null.
     * @param verb The HTTP request method. Cannot be null.
     * @param msg The information message associated with the request.
     * @param uiMsg The information message associated with the request, suited for rendering on a UI.
     * @param devMsg The detailed information associated with the request, suited for developers.
     *
     * @return The corresponding resource meta data.
     */
    protected EnumMap<MetaKey, String> createMetadata(URI uri, Integer status, String verb, String msg, String uiMsg, String devMsg) {
        EnumMap<MetaKey, String> meta = new EnumMap<>(MetaKey.class);

        meta.put(MetaKey.href, uri.toString());
        meta.put(MetaKey.status, Integer.toString(status));
        meta.put(MetaKey.verb, verb);
        meta.put(MetaKey.message, msg);
        meta.put(MetaKey.uiMessage, uiMsg);
        meta.put(MetaKey.devMessage, devMsg);

        return meta;
    }

    /**
     * Generates a list of endpoint help DTOs used to describe the major service endpoints.
     *
     * @param resourceTypes The resource classes to describe.
     *
     * @return The list of endpoint help DTOs.
     */
    protected static List<EndpointHelp> describeEndpoints(List<Class<? extends AbstractResource>> resourceTypes) {
        List<EndpointHelp> result = new LinkedList<>();

        if ((resourceTypes != null) &&!resourceTypes.isEmpty()) {
            result = resourceTypes.stream()
                                  .map((resourceClass) -> EndpointHelp.fromResourceClass(resourceClass))
                                  .filter((dto) -> (dto != null))
                                  .collect(Collectors.toList());
        }

        Collections.sort(result);

        return result;
    }

    /**
     * Generates help information for each method on the service interface. The root context shall return a null list. All other endpoints will
     * re-use this implementation.
     *
     * @return The service endpoint method help objects.
     */
    protected List<MethodHelp> describeMethods() {
        List<MethodHelp> result = new LinkedList<>();
        Path endpointPath = getClass().getAnnotation(Path.class);

        for (Method method : getClass().getDeclaredMethods()) {
            String parentPath = (endpointPath == null) ? null : endpointPath.value();
            MethodHelp methodHelpDto = MethodHelp.fromMethodClass(parentPath, method);

            if (methodHelpDto != null) {
                result.add(methodHelpDto);
            }
        }

        Collections.sort(result);

        return result;
    }

    /**
     * Converts an infraction entity to an infraction DTO.
     *
     * @param infraction
     *
     * @return The infraction DTO.
     */
    Infraction fromEntity(com.salesforce.dva.argus.entity.Infraction infraction) {
        Infraction result = Converter.fromEntity(Infraction.class, infraction);

        result.setUserId(infraction.getUser().getId());
        result.setUserName(infraction.getUser().getUserName());
        result.setPolicyId(infraction.getPolicy().getId());

        return result;
    }

    /**
     * Converts an policy entity to an policy DTO.
     *
     * @param policy
     *
     * @return The policy DTO.
     */
    Policy fromEntity(com.salesforce.dva.argus.entity.Policy policy) {
        Policy result = Converter.fromEntity(Policy.class, policy);

        result.setAggregator(policy.getAggregator());
        result.setTriggerType(policy.getTriggerType());
        result.setSuspensionLevels(policy.getSuspensionLevels().stream().map((l) -> fromEntity(l)).collect(Collectors.toList()));

        return result;
    }

    /**
     * Converts an suspension level entity to an suspension level DTO.
     *
     * @param level
     *
     * @return The suspension level DTO.
     */
    SuspensionLevel fromEntity(com.salesforce.dva.argus.entity.SuspensionLevel level) {
        SuspensionLevel result = Converter.fromEntity(SuspensionLevel.class, level);

        result.setPolicyId(level.getPolicy().getId());

        return result;
    }

    /**
     * Converts an user entity to an user DTO.
     *
     * @param user
     *
     * @return The user DTO.
     */
    User fromEntity(PrincipalUser user) {
        return Converter.fromEntity(User.class, user);
    }

    /**
     * Converts a metric entity to a metric transfer object.
     *
     * @param metric The metric entity. Cannot be null.
     * @param userName The userName. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     *
     * @return The metric transfer object.
     */
    Metric fromEntity(com.salesforce.dva.argus.entity.Metric metric, String userName, BigInteger policyId) {
        Metric result = new Metric();

        result.setPolicyId(policyId);
        result.setUserName(userName);
        result.setDatapoints(metric.getDatapoints()
                                   .entrySet()
                                   .stream()
                                   .collect(Collectors.toMap(e -> e.getKey(), e -> Double.valueOf(e.getValue()))));

        return result;
    }

    /**
     * Returns the help for the endpoint. For the context root, it will return the endpoint help for all major endpoints. For a specific endpoint it
     * will return the method help for the endpoint.
     *
     * @return Help object describing the service.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/help")
    public Map<String, List<? extends Object>> help() {
        Map<String, List<?>> result = new LinkedHashMap<>();
        result.put("endpoints", describeEndpoints(getEndpoints()));
        result.put("methods", describeMethods());
        return result;
    }

    /**
     * Throws a web application exception corresponding to a <tt>BAD_REQUEST</tt> status if the condition is not met.
     *
     * @param condition The boolean condition to check.
     * @param message The exception message.
     *
     */
    protected static void requireThat(boolean condition, String message) {
        requireThat(condition, message, Status.BAD_REQUEST);
    }

    /**
     * Throws a web application exception if the condition is not met.
     *
     * @param condition The boolean condition to check.
     * @param message The exception message.
     * @param status The status to reflect.
     *
     */
    protected static void requireThat(boolean condition, String message, Status status) {
        if (!condition) {
            throw new WebApplicationException(message, status);
        }
    }

    /**
     * Converts an infraction DTO to an infraction entity object.
     *
     * @param infraction
     *
     * @return The infraction entity.
     */
    com.salesforce.dva.argus.entity.Infraction toEntity(Infraction infraction) {
        com.salesforce.dva.argus.entity.Infraction result = Converter.toEntity(com.salesforce.dva.argus.entity.Infraction.class, infraction);
        PrincipalUser creator = (infraction.getCreatedById() == null) ? null : userService.findUserByPrimaryKey(infraction.getCreatedById());
        PrincipalUser modifier = (infraction.getModifiedById() == null) ? null : userService.findUserByPrimaryKey(infraction.getModifiedById());
        PrincipalUser user = (infraction.getUserId() == null) ? null : userService.findUserByPrimaryKey(infraction.getUserId());
        com.salesforce.dva.argus.entity.Policy policy = (infraction.getPolicyId() == null) ? null : waaSService.getPolicy(infraction.getPolicyId());

        result.setCreatedBy(creator);
        result.setModifiedBy(modifier);
        result.setUser(user);
        result.setPolicy(policy);

        return result;
    }

    /**
     * Converts a policy DTO to a policy entity object.
     *
     * @param policy
     *
     * @return The policy entity.
     */
    com.salesforce.dva.argus.entity.Policy toEntity(Policy policy) {
        com.salesforce.dva.argus.entity.Policy result = Converter.toEntity(com.salesforce.dva.argus.entity.Policy.class, policy);
        PrincipalUser creator = (policy.getCreatedById() == null) ? null : userService.findUserByPrimaryKey(policy.getCreatedById());
        PrincipalUser modifier = (policy.getModifiedById() == null) ? null : userService.findUserByPrimaryKey(policy.getModifiedById());

        result.setModifiedBy(modifier);
        result.setCreatedBy(creator);
        result.setSuspensionLevels(policy.getSuspensionLevels().stream().map(d->toEntity(d)).collect(Collectors.toList()));

        return result;
    }

    /**
     * Converts a suspension level DTO to a suspension level entity object.
     *
     * @param level
     *
     * @return The suspension level entity.
     */
    com.salesforce.dva.argus.entity.SuspensionLevel toEntity(SuspensionLevel level) {
        com.salesforce.dva.argus.entity.SuspensionLevel result = Converter.toEntity(com.salesforce.dva.argus.entity.SuspensionLevel.class, level);
        PrincipalUser creator = (level.getCreatedById() == null) ? null : userService.findUserByPrimaryKey(level.getCreatedById());
        PrincipalUser modifier = (level.getModifiedById() == null) ? null : userService.findUserByPrimaryKey(level.getModifiedById());
        com.salesforce.dva.argus.entity.Policy policy = (level.getPolicyId() == null) ? null : waaSService.getPolicy(level.getId());

        result.setModifiedBy(modifier);
        result.setCreatedBy(creator);
        result.setPolicy(policy);

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
     * Returns the logged in user object.
     *
     * @param req The HTTP request.  Cannot be null.
     *
     * @return The logged in user object.
     */
    protected PrincipalUser getRemoteUser(HttpServletRequest req) {
        requireArgument(req != null, "Request cannot be null.");

        PrincipalUser result = null;
        Object principalAttribute = req.getSession(true).getAttribute(AuthFilter.USER_ATTRIBUTE_NAME);

        if (principalAttribute != null) {
            User user = User.class.cast(principalAttribute);

            result = userService.findUserByUsername(user.getUserName());
        }

        return result;
    }

    /**
     * Used to provide description information about web service endpoints and methods.
     *
     * @author Tom Valine (tvaline@salesforce.com)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.METHOD, ElementType.TYPE })
    public static @interface Description {
        public String value();
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



