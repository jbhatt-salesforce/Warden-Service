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
import com.salesforce.dva.argus.service.WaaSService;
import com.salesforce.dva.argus.util.WaaSObjectConverter;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Subscription;
import com.salesforce.dva.warden.dto.SuspensionLevel;
import com.salesforce.dva.warden.dto.User;
import com.salesforce.dva.warden.dto.Resource.MetaKey;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.ws.dto.Converter;
import com.salesforce.dva.warden.ws.resources.AbstractResource.Description;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Warden web service resources.
 *
 * @author Ruofan Zhang (rzhang@salesforce.com)
 */
@Path("/policy")
@Description("Provides methods to manipulate warden entities.")
public class PolicyResource extends AbstractResource {

    static Policy fromEntity(com.salesforce.dva.argus.entity.Policy policy) {
        Policy result = Converter.fromEntity(Policy.class, policy);
        return result;
    }

    static Infraction fromEntity(com.salesforce.dva.argus.entity.Infraction infraction) {
        Infraction result = Converter.fromEntity(Infraction.class, infraction);
        return result;
    }

    private final WaaSService waaSService;

    @Context
    UriInfo uriInfo;

    public PolicyResource() {
        this.waaSService = system.getServiceFactory().getWaaSService();
    }

    //~ Methods **************************************************************************************************************************************
    //========================policies start from here============================
    /**
     * Returns the list of policies owned by the owner.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param username User name used for filtering the policies. It is optional.
     *
     * @return The list of policies filtered by user and owner.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy")
    @Description("Returns all policies.")
    public List<Resource<Policy>> getPolicies(@Context HttpServletRequest req,
            @QueryParam("username") String username) {

        List<Resource<Policy>> result = new ArrayList<>();
        List<com.salesforce.dva.argus.entity.Policy> policies = null;
        PrincipalUser remoteUser = getRemoteUser(req);

        //query policy from waasService
        if (username != null && !username.isEmpty()) {
            policies = waaSService.getPoliciesForUser(username);
        } else {
            policies = waaSService.getPolicies();
        }

        //filter policy based on ownership if remoteUser is not admin
        if (policies != null && !policies.isEmpty() && !remoteUser.isPrivileged()) {
            policies = policies.stream().filter(p -> p.getOwners().contains(remoteUser.getUserName())).collect(Collectors.toList());
        }

        if (policies == null || policies.isEmpty()) {
            throw new WebApplicationException("All policies you are querying cannot be found!", Response.Status.NOT_FOUND);
        }

        for (com.salesforce.dva.argus.entity.Policy p : policies) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(p.getId().toString()).build();

            int statusCode = (p == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (p == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Policy policyDto = (p == null) ? null : WaaSObjectConverter.convertToPolicyDto(p);
            Resource<Policy> res = new Resource<>();
            res.setEntity(policyDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Creates new policies.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policies The policy objects. Cannot be null.
     *
     * @return Created policy objects.
     *
     * @throws WebApplicationException The exception with 400 status will be thrown if the policy object is null.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/policy")
    @Description("Creates policies.")
    public List<Resource<Policy>> createPolicies(@Context HttpServletRequest req, List<Policy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new WebApplicationException("Null policy objects cannot be created.", Status.BAD_REQUEST);
        }

        List<Resource<Policy>> result = new ArrayList<Resource<Policy>>();

        PrincipalUser remoteUser = getRemoteUser(req);

        //filter uploaded policy based on ownership if remoteUser is not admin
        if (!remoteUser.isPrivileged()) {
            policies = policies.stream().filter(p -> p.getOwners().contains(remoteUser.getUserName())).collect(Collectors.toList());
        }

        if (policies == null || policies.isEmpty()) {
            throw new WebApplicationException("Remote user doesn't own these policies and has no priviledge to create them.", Status.BAD_REQUEST);
        }

        for (Policy p : policies) {
            com.salesforce.dva.argus.entity.Policy policyEntity = null;
            com.salesforce.dva.argus.entity.Policy entity = new com.salesforce.dva.argus.entity.Policy(remoteUser, p.getService(), p.getName(), p.getOwners(),
                    p.getUsers(), p.getTriggerType(), p.getAggregator(), p.getThresholds(), p.getTimeUnit(),
                    p.getDefaultValue(), p.getCronEntry());
            try {
                policyEntity = waaSService.updatePolicy(entity);
            } catch (Exception e) {
                policyEntity = null;
            }

            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = (policyEntity == null) ? ub.build() : ub.path(policyEntity.getId().toString()).build();

            int statusCode = (policyEntity == null) ? Response.Status.BAD_REQUEST.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (policyEntity == null) ? Response.Status.BAD_REQUEST.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Policy policyDto = (policyEntity == null) ? null : WaaSObjectConverter.convertToPolicyDto(policyEntity);

            Resource<Policy> res = new Resource<>();
            res.setEntity(policyDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Deletes policies.
     *
     * @param req The HttpServlet request object. Cannot be null.
     *
     * @return REST response indicating whether the policies deletion was successful.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if an policy does not exist.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy")
    @Description("Deletes selected policies owned by this user.")
    public List<Resource<Policy>> deletePolicies(@Context HttpServletRequest req, @QueryParam("id") List<BigInteger> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            throw new WebApplicationException("Policy ids are needed for deletion.", Status.BAD_REQUEST);
        }
        List<Resource<Policy>> result = new ArrayList<Resource<Policy>>();
        PrincipalUser remoteUser = getRemoteUser(req);

        boolean unauthorized = false;
        for (BigInteger id : policyIds) {
            int statusCode = Response.Status.NOT_FOUND.getStatusCode();
            String message = Response.Status.NOT_FOUND.getReasonPhrase();

            com.salesforce.dva.argus.entity.Policy delPolicy = waaSService.getPolicy(id);
            if (delPolicy != null) {
                if (remoteUser.isPrivileged() || delPolicy.getOwners().contains(remoteUser.getUserName())) {
                    waaSService.deletePolicy(delPolicy);

                    statusCode = Response.Status.OK.getStatusCode();
                    message = Response.Status.OK.getReasonPhrase();
                } else {
                    unauthorized = true;

                    statusCode = Response.Status.UNAUTHORIZED.getStatusCode();
                    message = Response.Status.UNAUTHORIZED.getReasonPhrase();
                }
            }
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = (delPolicy == null) ? ub.build() : ub.path(id.toString()).build();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Policy policyDto = (delPolicy == null || unauthorized == true) ? null : WaaSObjectConverter.convertToPolicyDto(delPolicy);

            Resource<Policy> res = new Resource<>();
            res.setEntity(policyDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Updates existing policies.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policies Policies objects. Cannot be null.
     *
     * @return Updated policy objects.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if the alert does not exist.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/policy")
    @Description("Updates policy objects")
    public List<Resource<Policy>> updatePolicies(@Context HttpServletRequest req, List<Policy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new WebApplicationException("Null policy objects cannot be updated.", Status.BAD_REQUEST);
        }

        List<Resource<Policy>> result = new ArrayList<Resource<Policy>>();

        PrincipalUser remoteUser = getRemoteUser(req);

        // filter uploaded policy based on ownership if remoteUser is not admin
        if (!remoteUser.isPrivileged()) {
            policies = policies.stream().filter(p -> p.getOwners().contains(remoteUser.getUserName()))
                    .collect(Collectors.toList());
        }

        if (policies == null || policies.isEmpty()) {
            throw new WebApplicationException(
                    "Remote user doesn't own these policies and has no priviledge to update them.", Status.BAD_REQUEST);
        }

        for (Policy newPolicy : policies) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(newPolicy.getId().toString()).build();

            com.salesforce.dva.argus.entity.Policy oldPolicy = waaSService.getPolicy(newPolicy.getId());
            int statusCode = (oldPolicy == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (oldPolicy == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);
            if (oldPolicy != null) {
                oldPolicy.setOwners(newPolicy.getOwners());
                oldPolicy.setUsers(newPolicy.getUsers());
                oldPolicy.setTriggerType(newPolicy.getTriggerType());
                oldPolicy.setAggregator(newPolicy.getAggregator());
                oldPolicy.setThreshold(newPolicy.getThresholds());
                oldPolicy.setTimeUnit(newPolicy.getTimeUnit());
                oldPolicy.setDefaultValue(newPolicy.getDefaultValue());
                oldPolicy.setCronEntry(newPolicy.getCronEntry());

                oldPolicy.setModifiedBy(remoteUser);
            }

            com.salesforce.dva.argus.entity.Policy updatedOldPolicy = waaSService.updatePolicy(oldPolicy);
            Policy policyDto = (updatedOldPolicy == null) ? null : WaaSObjectConverter.convertToPolicyDto(updatedOldPolicy);
            Resource<Policy> res = new Resource<>();
            res.setEntity(policyDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    //=======================policy start from here====================================
    /**
     * Finds a policy by policy ID.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid ID of a policy. Cannot be null and must be a positive non-zero number.
     *
     * @return Policy
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if a policy does not exist.
     */
    /*    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}")
    @Description("Returns a policy by its ID.")
	public Resource<Policy> getPolicyById(@Context HttpServletRequest req, @PathParam("pid") BigInteger pid) {
		if (pid == null || pid.compareTo(BigInteger.ZERO) < 1) {
			throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.",
					Status.BAD_REQUEST);
		}

		Resource<Policy> result = null;
		com.salesforce.dva.argus.entity.Policy policy = null;
		PrincipalUser remoteUser = getRemoteUser(req);

		policy = waaSService.getPolicy(pid);
		
		if (policy == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);

		// check ownership
		if (!remoteUser.isPrivileged() && !policy.getOwners().contains(remoteUser.getUserName())) {
			throw new WebApplicationException("Remote user doesn't own this policy and has no priviledge to access it.",
					Status.BAD_REQUEST);
		}

		EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);
    	
    	UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();        
        
    	int statusCode = (policy == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
    	String message = (policy == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();
    	
    	meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.STATUS, Integer.toString(statusCode));
        meta.put(MetaKey.VERB, req.getMethod());
        meta.put(MetaKey.MESSAGE, message);
        meta.put(MetaKey.UI_MESSAGE, message);
        meta.put(MetaKey.DEV_MESSAGE, message);              
            	       	
    	Policy policyDto = (policy == null) ? null : WaaSObjectConverter.convertToPolicyDto(policy);
    	result = new Resource<Policy>();
    	result.setEntity(policyDto);
    	result.setMeta(meta);
		
		return result;
	}
     */
    /**
     * Finds a policy by policy service and name.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid ID of a policy. Cannot be null and must be a positive non-zero number.
     *
     * @return Policy
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if a policy does not exist.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}")
    @Description("Returns a policy by its ID.")
    public Resource<Policy> getPolicyById(@Context HttpServletRequest req, @QueryParam("service") String service, @QueryParam("name") String name) {

        Resource<Policy> result = null;
        com.salesforce.dva.argus.entity.Policy policy = null;
        PrincipalUser remoteUser = getRemoteUser(req);

        policy = waaSService.getPolicy(service, name);

        if (policy == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        // check ownership
        if (!remoteUser.isPrivileged() && !policy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't own this policy and has no priviledge to access it.",
                    Status.BAD_REQUEST);
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        int statusCode = (policy == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
        String message = (policy == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        Policy policyDto = (policy == null) ? null : WaaSObjectConverter.convertToPolicyDto(policy);
        result = new Resource<Policy>();
        result.setEntity(policyDto);
        result.setMeta(meta);

        return result;
    }

    /**
     * Deletes the policy.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid The policy Id. Cannot be null and must be a positive non-zero number.
     *
     * @return REST response indicating whether the policy deletion was successful.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if an policy does not exist.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}")
    @Description("Deletes the policy having the given ID.")
    public Resource<Policy> deletePolicy(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger pid) {
        if (pid == null || pid.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(pid);

        if (waaSService.getLevels(existingPolicy) != null && !waaSService.getLevels(existingPolicy).isEmpty()) {
            throw new WebApplicationException("This policy has suspension levels associated with it, please delete the suspension levels first!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);

        int statusCode = Response.Status.NOT_FOUND.getStatusCode();
        String message = Response.Status.NOT_FOUND.getReasonPhrase();

        boolean unauthorized = false;

        if (existingPolicy != null) {
            if (remoteUser.isPrivileged() || existingPolicy.getOwners().contains(remoteUser.getUserName())) {
                waaSService.deletePolicy(existingPolicy);

                statusCode = Response.Status.OK.getStatusCode();
                message = Response.Status.OK.getReasonPhrase();
            } else {
                unauthorized = true;

                statusCode = Response.Status.UNAUTHORIZED.getStatusCode();
                message = Response.Status.UNAUTHORIZED.getReasonPhrase();
            }
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        Policy policyDto = (existingPolicy == null || unauthorized == true) ? null : WaaSObjectConverter.convertToPolicyDto(existingPolicy);

        Resource<Policy> res = new Resource<>();
        res.setEntity(policyDto);
        res.setMeta(meta);
        return res;
    }

    /**
     * Updates existing policy.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The id of an policy. Cannot be null.
     * @param policy The new policy object. Cannot be null.
     *
     * @return Updated policy object.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if the alert does not exist.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}")
    @Description("Updates an policy having the given ID.")
    public Resource<Policy> updatePolicy(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger pid, Policy policy) {
        if (pid == null || pid.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (policy == null) {
            throw new WebApplicationException("Null object cannot be updated.", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);

        //check ownership
        if (!remoteUser.isPrivileged() && !policy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't own this policy and has no priviledge to access it.", Status.BAD_REQUEST);
        }

        Resource<Policy> result = null;
        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(pid);

        if (existingPolicy == null) {
            throw new WebApplicationException("This policy doesn't exist, please create it first!", Response.Status.NOT_FOUND);
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        int statusCode = (existingPolicy == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
        String message = (existingPolicy == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        if (existingPolicy != null) {
            existingPolicy.setOwners(policy.getOwners());
            existingPolicy.setUsers(policy.getUsers());
            existingPolicy.setTriggerType(policy.getTriggerType());
            existingPolicy.setAggregator(policy.getAggregator());
            existingPolicy.setThreshold(policy.getThresholds());
            existingPolicy.setTimeUnit(policy.getTimeUnit());
            existingPolicy.setDefaultValue(policy.getDefaultValue());
            existingPolicy.setCronEntry(policy.getCronEntry());

            existingPolicy.setModifiedBy(remoteUser);
        }

        com.salesforce.dva.argus.entity.Policy updatedOldPolicy = waaSService.updatePolicy(existingPolicy);
        Policy policyDto = (updatedOldPolicy == null) ? null : WaaSObjectConverter.convertToPolicyDto(updatedOldPolicy);
        result = new Resource<Policy>();
        result.setEntity(policyDto);
        result.setMeta(meta);
        return result;
    }

    //==============levels start from here=================
    /**
     * Returns the list of suspension levels owned by the owner with given policy id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param username User name used for filtering the policies. It is optional.
     *
     * @return The list of levels filtered by user and owner.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/level")
    @Description("Returns all levels with policy id.")
    public List<Resource<SuspensionLevel>> getLevels(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @QueryParam("username") String username) {

        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying levels!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query suspension levels for this policy!", Status.BAD_REQUEST);
        }

        if (username != null && !existingPolicy.getUsers().contains(username)) {
            throw new WebApplicationException("Query user doesn't subject to this policy!", Status.BAD_REQUEST);
        }

        List<Resource<SuspensionLevel>> result = new ArrayList<Resource<SuspensionLevel>>();
        List<com.salesforce.dva.argus.entity.SuspensionLevel> levels = null;

        levels = waaSService.getLevels(existingPolicy);

        if (levels == null || levels.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        for (com.salesforce.dva.argus.entity.SuspensionLevel l : levels) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(l.getId().toString()).build();

            int statusCode = (l == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (l == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            SuspensionLevel levelDto = (l == null) ? null : WaaSObjectConverter.convertToLevelDto(l);
            Resource<SuspensionLevel> res = new Resource<>();
            res.setEntity(levelDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Creates new levels for existing policy.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid The policy id. Cannot be null.
     * @param levels	New levels to be created. Cannot be null.
     *
     * @return Created suspensionlevel objects.
     *
     * @throws WebApplicationException The exception with 400 status will be thrown if the policy object is null.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/level")
    @Description("Creates levels for a specific policy.")
    public List<Resource<SuspensionLevel>> createLevels(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            List<SuspensionLevel> levels) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (levels == null || levels.isEmpty()) {
            throw new WebApplicationException("Null suspension level objects cannot be created.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying levels!", Status.BAD_REQUEST);
        }

        List<Resource<SuspensionLevel>> result = new ArrayList<Resource<SuspensionLevel>>();

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException(
                    "Remote user doesn't have priveilege to query suspension levels for this policy!",
                    Status.BAD_REQUEST);
        }

        for (SuspensionLevel level : levels) {
            com.salesforce.dva.argus.entity.SuspensionLevel levelEntity = null;
            com.salesforce.dva.argus.entity.SuspensionLevel newLevel = new com.salesforce.dva.argus.entity.SuspensionLevel(
                    remoteUser, existingPolicy, level.getLevelNumber(), level.getInfractionCount(),
                    level.getSuspensionTime().longValue());
            try {
                levelEntity = waaSService.updateLevel(newLevel);
            } catch (Exception e) {
                levelEntity = null;
            }

            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(levelEntity.getId().toString()).build();

            int statusCode = (levelEntity == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (levelEntity == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            SuspensionLevel levelDto = (levelEntity == null) ? null : WaaSObjectConverter.convertToLevelDto(levelEntity);

            Resource<SuspensionLevel> res = new Resource<>();
            res.setEntity(levelDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Deletes levels.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid The policy Id. Cannot be null and must be a positive non-zero number.
     *
     * @return REST response indicating whether the suspension levels deletion was successful.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if an policy does not exist.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/level")
    @Description("Deletes the all the policies owned by this user.")
    public List<Resource<SuspensionLevel>> deleteLevels(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @QueryParam("id") List<BigInteger> levelIds) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for deleting levels!", Status.BAD_REQUEST);
        }

        if (levelIds == null || levelIds.isEmpty()) {
            throw new WebApplicationException("Level ids are needed for deletion.", Status.BAD_REQUEST);
        }

        List<Resource<SuspensionLevel>> result = new ArrayList<Resource<SuspensionLevel>>();
        PrincipalUser remoteUser = getRemoteUser(req);

        boolean unauthorized = false;

        for (BigInteger id : levelIds) {
            int statusCode = Response.Status.NOT_FOUND.getStatusCode();
            String message = Response.Status.NOT_FOUND.getReasonPhrase();

            com.salesforce.dva.argus.entity.SuspensionLevel delLevel = waaSService.getLevel(existingPolicy, id);
            if (delLevel != null) {
                if (remoteUser.isPrivileged() || existingPolicy.getOwners().contains(remoteUser.getUserName())) {
                    waaSService.deleteLevel(delLevel);

                    statusCode = Response.Status.OK.getStatusCode();
                    message = Response.Status.OK.getReasonPhrase();
                } else {
                    unauthorized = true;

                    statusCode = Response.Status.UNAUTHORIZED.getStatusCode();
                    message = Response.Status.UNAUTHORIZED.getReasonPhrase();
                }
            }

            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = (delLevel == null) ? ub.build() : ub.path(id.toString()).build();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            SuspensionLevel levelDto = (delLevel == null || unauthorized == true) ? null : WaaSObjectConverter.convertToLevelDto(delLevel);

            Resource<SuspensionLevel> res = new Resource<>();
            res.setEntity(levelDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Updates existing levels for given policy id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policies Policies objects. Cannot be null.
     *
     * @return Updated policy objects.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if the alert does not exist.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/level")
    @Description("Updates policy objects")
    public List<Resource<SuspensionLevel>> updateLevels(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            List<SuspensionLevel> levels) {

        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.",
                    Status.BAD_REQUEST);
        }
        if (levels == null || levels.isEmpty()) {
            throw new WebApplicationException("Null suspension level objects cannot be created.", Status.BAD_REQUEST);
        }
        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for updating levels!", Status.BAD_REQUEST);
        }

        List<Resource<SuspensionLevel>> result = new ArrayList<Resource<SuspensionLevel>>();

        PrincipalUser remoteUser = getRemoteUser(req);

        if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException(
                    "Remote user doesn't have priveilege to update suspension levels for this policy!",
                    Status.BAD_REQUEST);
        }

        for (SuspensionLevel newLevel : levels) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(newLevel.getId().toString()).build();

            com.salesforce.dva.argus.entity.SuspensionLevel oldLevel = waaSService.getLevel(existingPolicy,
                    newLevel.getId());
            int statusCode = (oldLevel == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (oldLevel == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            if (oldLevel != null) {
                oldLevel.setLevelNumber(newLevel.getLevelNumber());
                oldLevel.setInfractionCount(newLevel.getInfractionCount());
                oldLevel.setSuspensionTime(newLevel.getSuspensionTime().longValue());

                oldLevel.setModifiedBy(remoteUser);
            }

            com.salesforce.dva.argus.entity.SuspensionLevel updatedOldLevel = waaSService.updateLevel(oldLevel);
            SuspensionLevel levelDto = (updatedOldLevel == null) ? null : WaaSObjectConverter.convertToLevelDto(updatedOldLevel);
            Resource<SuspensionLevel> res = new Resource<>();
            res.setEntity(levelDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    //==========================level start from here================================
    /**
     * Finds a suspension level by policy ID and level id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid ID of a policy. Cannot be null and must be a positive non-zero number.
     * @param levelid ID of suspension level. Cannot be null and must be a positive non-zero number.
     * @return Policy
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if a policy does not exist.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/level/{levelid}")
    @Description("Returns a policy by its ID.")
    public Resource<SuspensionLevel> getLevel(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("levelid") BigInteger levelId) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (levelId == null || levelId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Suspension Level Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying levels!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query suspension levels for this policy!", Status.BAD_REQUEST);
        }

        Resource<SuspensionLevel> result = null;
        com.salesforce.dva.argus.entity.SuspensionLevel level = waaSService.getLevel(existingPolicy, levelId);
        if (level == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        int statusCode = (level == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
        String message = (level == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        SuspensionLevel levelDto = (level == null) ? null : WaaSObjectConverter.convertToLevelDto(level);
        result = new Resource<SuspensionLevel>();
        result.setEntity(levelDto);
        result.setMeta(meta);
        return result;
    }

    /**
     * Deletes suspension level.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid The policy Id. Cannot be null and must be a positive non-zero number.
     * @param levelid	The suspension level id. Cannot be null and must be a positive non-zero number.
     *
     * @return REST response indicating whether the suspension level deletion was successful.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if a suspension level does not exist.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/level/{levelid}")
    @Description("Deletes the suspension level with policy id and level id.")
    public Resource<SuspensionLevel> deleteLevel(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("levelid") BigInteger levelId) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (levelId == null || levelId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Suspension Level Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for deleting levels!", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.SuspensionLevel existingLevel = waaSService.getLevel(existingPolicy, levelId);

        PrincipalUser remoteUser = getRemoteUser(req);

        int statusCode = Response.Status.NOT_FOUND.getStatusCode();
        String message = Response.Status.NOT_FOUND.getReasonPhrase();

        boolean unauthorized = false;

        if (existingLevel != null) {
            if (remoteUser.isPrivileged() || existingPolicy.getOwners().contains(remoteUser.getUserName())) {
                waaSService.deleteLevel(existingLevel);

                statusCode = Response.Status.OK.getStatusCode();
                message = Response.Status.OK.getReasonPhrase();
            } else {
                unauthorized = true;

                statusCode = Response.Status.UNAUTHORIZED.getStatusCode();
                message = Response.Status.UNAUTHORIZED.getReasonPhrase();
            }
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        SuspensionLevel levelDto = (existingLevel == null || unauthorized == true) ? null : WaaSObjectConverter.convertToLevelDto(existingLevel);

        Resource<SuspensionLevel> res = new Resource<>();
        res.setEntity(levelDto);
        res.setMeta(meta);
        return res;
    }

    /**
     * Updates existing suspension level with given policy id and level id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The id of an policy. Cannot be null.
     * @param levelId	The id of suspension level. Cannot be null.
     * @param policy The new suspension level object. Cannot be null.
     *
     * @return Updated suspension level object.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if the suspension level does not exist.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/level/{levelid}")
    @Description("Updates an suspension level having the given policy ID and level ID.")
    public Resource<SuspensionLevel> updateLevel(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("levelid") BigInteger levelId,
            SuspensionLevel level) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (levelId == null || levelId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Suspension Level Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying levels!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have priveilege to update suspension levels for this policy!", Status.BAD_REQUEST);
        }

        Resource<SuspensionLevel> result = null;
        com.salesforce.dva.argus.entity.SuspensionLevel oldLevel = waaSService.getLevel(existingPolicy, levelId);
        if (oldLevel == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        int statusCode = (level == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
        String message = (level == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        if (oldLevel != null) {
            oldLevel.setLevelNumber(level.getLevelNumber());
            oldLevel.setInfractionCount(level.getInfractionCount());
            oldLevel.setSuspensionTime(level.getSuspensionTime().longValue());

            oldLevel.setModifiedBy(remoteUser);
        }

        com.salesforce.dva.argus.entity.SuspensionLevel updatedOldLevel = waaSService.updateLevel(oldLevel);
        SuspensionLevel levelDto = (level == null) ? null : WaaSObjectConverter.convertToLevelDto(updatedOldLevel);
        result = new Resource<SuspensionLevel>();
        result.setEntity(levelDto);
        result.setMeta(meta);
        return result;
    }

    //=================infraction(s) start from here==================
    /**
     * Returns all infractions with given policy id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param username User name used for filtering the infraction. It is optional.
     *
     * @return The list of infraction filtered by policy and user.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/infraction")
    @Description("Returns all infractions with policy id.")
    public List<Resource<Infraction>> getInfractions(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @QueryParam("username") String username) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying infrations!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query infrations for this policy!", Status.BAD_REQUEST);
        }

        if (username != null && !existingPolicy.getUsers().contains(username)) {
            throw new WebApplicationException("Query user doesn't subject to this policy!", Status.BAD_REQUEST);
        }

        List<Resource<Infraction>> result = new ArrayList<Resource<Infraction>>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = null;

        if (username != null && !username.isEmpty()) {
            infractions = waaSService.getInfractionsByPolicyAndUserName(existingPolicy, username);
        } else {
            infractions = waaSService.getInfractions(existingPolicy);
        }

        if (infractions == null || infractions.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        for (com.salesforce.dva.argus.entity.Infraction inf : infractions) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(inf.getId().toString()).build();

            int statusCode = (inf == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (inf == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Infraction infDto = (inf == null) ? null : WaaSObjectConverter.convertToInfractionDto(inf);
            Resource<Infraction> res = new Resource<>();
            res.setEntity(infDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Finds a infraction by policy ID and infracction id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid ID of a policy. Cannot be null and must be a positive non-zero number.
     * @param iid ID of an infraction. Cannot be null and must be a positive non-zero number.
     * @return Infraction
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if a policy does not exist.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/infraction/{iid}")
    @Description("Returns an infraction by policy ID and its ID.")
    public Resource<Infraction> getInfraction(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("iid") BigInteger infractionId) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (infractionId == null || infractionId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Infraction Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying infractions!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query any infraction for this policy!", Status.BAD_REQUEST);
        }

        Resource<Infraction> result = null;
        com.salesforce.dva.argus.entity.Infraction infraction = waaSService.getInfraction(existingPolicy, infractionId);
        if (infraction == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        int statusCode = (infraction == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
        String message = (infraction == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        Infraction infractionDto = (infraction == null) ? null : WaaSObjectConverter.convertToInfractionDto(infraction);
        result = new Resource<Infraction>();
        result.setEntity(infractionDto);
        result.setMeta(meta);
        return result;
    }

    //=============Suspension start from here=================
    /**
     * Returns all infractions with given policy id and user name if suspension happens.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid ID of a policy. Cannot be null and must be a positive non-zero number.
     * @param uname user name of an warden user. Cannot be null or empty.
     * @return Infraction list
     *
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/user/{uname}/suspension")
    @Description("Returns all infractions with policy id and user name if suspension happens.")
    public List<Resource<Infraction>> getSuspensionForUserAndPolicy(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("uname") String userName) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.",
                    Status.BAD_REQUEST);
        }

        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User Id cannot be null or an empty string.",
                    Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying suspension!", Status.BAD_REQUEST);
        }

        PrincipalUser existingUser = userService.findUserByUsername(userName);

        if (existingUser == null) {
            throw new WebApplicationException("User doesn't exist for querying infractions!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query anything under this user!", Status.BAD_REQUEST);
        } else if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query infraction for this policy!", Status.BAD_REQUEST);
        }

        if (existingUser.getUserName() != null && !existingPolicy.getUsers().contains(existingUser.getUserName())) {
            throw new WebApplicationException("Query user doesn't subject to this policy!", Status.BAD_REQUEST);
        }

        List<Resource<Infraction>> result = new ArrayList<Resource<Infraction>>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = waaSService.getInfractionsByPolicyAndUserName(existingPolicy, existingUser.getUserName());

        if (infractions == null || infractions.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        List<com.salesforce.dva.argus.entity.Infraction> suspensions = infractions;
        if (suspensions != null && !suspensions.isEmpty()) {
            suspensions = suspensions.stream().filter(i -> (i.getExpirationTimestamp() != null) && (!i.getExpirationTimestamp().equals(0L))).collect(Collectors.toList());
        }
        if (suspensions == null || suspensions.isEmpty()) {
            throw new WebApplicationException("This user doesn't has suspension for this policy.", Status.BAD_REQUEST);
        }

        for (com.salesforce.dva.argus.entity.Infraction s : suspensions) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(s.getId().toString()).build();

            int statusCode = (s == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (s == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Infraction suspensionDto = (s == null) ? null : WaaSObjectConverter.convertToInfractionDto(s);
            Resource<Infraction> res = new Resource<>();
            res.setEntity(suspensionDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Deletes suspension for user.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid The policy Id. Cannot be null and must be a positive non-zero number.
     * @param uname	The user name. Cannot be null or an empty string.
     *
     * @return REST response indicating whether the suspension deletion was successful.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if an policy does not exist.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/user/{uname}/suspension")
    @Description("Deletes all infractions with policy id and user id if suspension happens.")
    public List<Resource<Infraction>> deleteSuspensionForUser(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("uname") String userName,
            @QueryParam("id") List<BigInteger> infractionIds) {

        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User name cannot be null or an empty string.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for deleting suspensions!", Status.BAD_REQUEST);
        }

        PrincipalUser existingUser = userService.findUserByUsername(userName);

        if (existingUser == null) {
            throw new WebApplicationException("User doesn't exist for querying infractions!", Status.BAD_REQUEST);
        }

        if (infractionIds == null || infractionIds.isEmpty()) {
            throw new WebApplicationException("Infraction ids are needed for deletion.", Status.BAD_REQUEST);
        }

        List<Resource<Infraction>> result = new ArrayList<Resource<Infraction>>();
        PrincipalUser remoteUser = getRemoteUser(req);

        boolean unauthorized = false;

        for (BigInteger id : infractionIds) {
            int statusCode = Response.Status.NOT_FOUND.getStatusCode();
            String message = Response.Status.NOT_FOUND.getReasonPhrase();
            com.salesforce.dva.argus.entity.Infraction delInfraction = waaSService.getInfraction(existingPolicy, id);

            if (delInfraction != null) {
                if (remoteUser.isPrivileged() || existingPolicy.getOwners().contains(remoteUser.getUserName())) {
                    waaSService.deleteInfraction(delInfraction);

                    statusCode = Response.Status.OK.getStatusCode();
                    message = Response.Status.OK.getReasonPhrase();
                } else {
                    unauthorized = true;

                    statusCode = Response.Status.UNAUTHORIZED.getStatusCode();
                    message = Response.Status.UNAUTHORIZED.getReasonPhrase();
                }
            }

            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = (delInfraction == null) ? ub.build() : ub.path(id.toString()).build();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Infraction infractionDto = (delInfraction == null || unauthorized == true) ? null : WaaSObjectConverter.convertToInfractionDto(delInfraction);

            Resource<Infraction> res = new Resource<>();
            res.setEntity(infractionDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Returns all infractions with given policy id if suspension happens.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid ID of a policy. Cannot be null and must be a positive non-zero number.
     * @return Infraction list
     *
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/suspension")
    @Description("Returns all infractions with policy id and user id if suspension happens.")
    public List<Resource<Infraction>> getSuspension(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId) {

        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.",
                    Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying suspension!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query infraction for this policy!", Status.BAD_REQUEST);
        }

        List<Resource<Infraction>> result = new ArrayList<Resource<Infraction>>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = waaSService.getInfractions(existingPolicy);

        if (infractions == null || infractions.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        if (infractions != null && !infractions.isEmpty()) {
            infractions = infractions.stream().filter(i -> (i.getExpirationTimestamp() != null) && (!i.getExpirationTimestamp().equals(0L))).collect(Collectors.toList());
        }
        if (infractions == null || infractions.isEmpty()) {
            throw new WebApplicationException("This user doesn't has suspension for this policy.", Status.BAD_REQUEST);
        }

        for (com.salesforce.dva.argus.entity.Infraction s : infractions) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(s.getId().toString()).build();

            int statusCode = (s == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (s == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Infraction suspensionDto = (s == null) ? null : WaaSObjectConverter.convertToInfractionDto(s);
            Resource<Infraction> res = new Resource<>();
            res.setEntity(suspensionDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Deletes suspension for policy.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid The policy Id. Cannot be null and must be a positive non-zero number.
     *
     * @return REST response indicating whether the suspension deletion was successful.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if an policy does not exist.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/suspension")
    @Description("Deletes all infractions with policy id and user id if suspension happens.")
    public List<Resource<Infraction>> deleteSuspension(@Context HttpServletRequest req, @PathParam("pid") BigInteger policyId,
            @QueryParam("id") List<BigInteger> infractionIds) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for deleting suspensions!", Status.BAD_REQUEST);
        }

        if (infractionIds == null || infractionIds.isEmpty()) {
            throw new WebApplicationException("Infraction ids are needed for deletion.", Status.BAD_REQUEST);
        }

        List<Resource<Infraction>> result = new ArrayList<Resource<Infraction>>();
        PrincipalUser remoteUser = getRemoteUser(req);

        boolean unauthorized = false;

        for (BigInteger id : infractionIds) {
            int statusCode = Response.Status.NOT_FOUND.getStatusCode();
            String message = Response.Status.NOT_FOUND.getReasonPhrase();
            com.salesforce.dva.argus.entity.Infraction delInfraction = waaSService.getInfraction(existingPolicy, id);

            if (delInfraction != null) {
                if (remoteUser.isPrivileged() || existingPolicy.getOwners().contains(remoteUser.getUserName())) {
                    waaSService.deleteInfraction(delInfraction);

                    statusCode = Response.Status.OK.getStatusCode();
                    message = Response.Status.OK.getReasonPhrase();
                } else {
                    unauthorized = true;

                    statusCode = Response.Status.UNAUTHORIZED.getStatusCode();
                    message = Response.Status.UNAUTHORIZED.getReasonPhrase();
                }
            }
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = (delInfraction == null) ? ub.build() : ub.path(id.toString()).build();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Infraction infractionDto = (delInfraction == null || unauthorized == true) ? null : WaaSObjectConverter.convertToInfractionDto(delInfraction);

            Resource<Infraction> res = new Resource<>();
            res.setEntity(infractionDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    //================user get starts from here===============================
    /**
     * Returns all warden users, only admin have this privilege
     *
     * @param req The HTTP request.
     *
     * @return The corresponding warden user DTOs.
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user")
    @Description("Returns the user having the given ID.")
    public List<Resource<User>> getUsers(@Context HttpServletRequest req) {
        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged()) {
            throw new WebApplicationException("Remote user doesn't have privilege to query all principal users!", Status.BAD_REQUEST);
        }

        List<Resource<User>> result = new ArrayList<Resource<User>>();
        List<PrincipalUser> users = userService.getPrincipalUsers();

        if (users == null || users.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        };
        for (com.salesforce.dva.argus.entity.PrincipalUser user : users) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(user.getId().toString()).build();

            int statusCode = (user == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (user == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            User userDto = (user == null) ? null : WaaSObjectConverter.convertToWardenUserDto(user);
            Resource<User> res = new Resource<>();
            res.setEntity(userDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Returns the user having the given user name.
     *
     * @param req The HTTP request.
     * @param userName The user name to retrieve
     *
     * @return The corresponding warden user DTO.
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user/{uname}")
    @Description("Returns the user having the given ID.")
    public Resource<User> getUserByName(@Context HttpServletRequest req,
            @PathParam("uname") final String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User name cannot be null or empty string.", Status.BAD_REQUEST);
        }
        PrincipalUser remoteUser = getRemoteUser(req);

        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query this user with id!", Status.BAD_REQUEST);
        }

        Resource<User> result = null;
        PrincipalUser user = userService.findUserByUsername(userName);
        if (user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        int statusCode = (user == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
        String message = (user == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        User userDto = (user == null) ? null : WaaSObjectConverter.convertToWardenUserDto(user);
        result = new Resource<User>();
        result.setEntity(userDto);
        result.setMeta(meta);

        return result;
    }

    /**
     * Returns all the policies for a specific user based on user name.
     *
     * @param req The HTTP request.
     * @param uname The user name to retrieve policies
     *
     * @return The policy list.
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user/{uname}/policy")
    @Description("Returns the policies for this user.")
    public List<Resource<Policy>> getPoliciesForUser(@Context HttpServletRequest req,
            @PathParam("uname") final String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User name cannot be null or an empty string.", Status.BAD_REQUEST);
        }
        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query policies with this user id!", Status.BAD_REQUEST);
        }

        PrincipalUser user = userService.findUserByUsername(userName);
        if (user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        return this.getPolicies(req, user.getUserName());
    }

    /**
     * Returns all the infractions for a specific user with user name and policy.
     *
     * @param req The HTTP request.
     * @param uname The user name to retrieve infrations
     * @param pid	The policy ID to retrieve infractions
     *
     * @return The infraction list.
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user/{uname}/policy/{pid}/infraction")
    @Description("Returns the policies for this user.")
    public List<Resource<Infraction>> getInfractionsForUserAndPolicy(@Context HttpServletRequest req,
            @PathParam("uanme") final String userName,
            @PathParam("pid") final BigInteger policyId) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User name cannot be null or an empty string.", Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying suspension!", Status.BAD_REQUEST);
        }

        PrincipalUser existingUser = userService.findUserByUsername(userName);

        if (existingUser == null) {
            throw new WebApplicationException("User doesn't exist for querying infractions!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query anything under this user!", Status.BAD_REQUEST);
        } else if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query infraction for this policy!", Status.BAD_REQUEST);
        }

        if (existingUser.getUserName() != null && !existingPolicy.getUsers().contains(existingUser.getUserName())) {
            throw new WebApplicationException("Query user doesn't subject to this policy!", Status.BAD_REQUEST);
        }

        List<Resource<Infraction>> result = new ArrayList<Resource<Infraction>>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = waaSService
                .getInfractionsByPolicyAndUserName(existingPolicy, existingUser.getUserName());

        if (infractions == null || infractions.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        for (com.salesforce.dva.argus.entity.Infraction inf : infractions) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(inf.getId().toString()).build();

            int statusCode = (inf == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (inf == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Infraction infDto = (inf == null) ? null : WaaSObjectConverter.convertToInfractionDto(inf);
            Resource<Infraction> res = new Resource<>();
            res.setEntity(infDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Returns all the infractions for a specific user based on user name.
     *
     * @param req The HTTP request.
     * @param uname The user name to retrieve policies
     *
     * @return The policy list.
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user/{uname}/infraction")
    @Description("Returns the infractions for this user.")
    public List<Resource<Infraction>> getInfractionsForUser(@Context HttpServletRequest req,
            @PathParam("uname") final String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User Id cannot be null or an empty string.", Status.BAD_REQUEST);
        }
        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query policies with this user id!", Status.BAD_REQUEST);
        }

        PrincipalUser user = userService.findUserByUsername(userName);
        if (user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        List<Resource<Infraction>> result = new ArrayList<Resource<Infraction>>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = waaSService.getInfractionsByUser(user);

        if (infractions == null || infractions.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        for (com.salesforce.dva.argus.entity.Infraction inf : infractions) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(inf.getId().toString()).build();

            int statusCode = (inf == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (inf == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Infraction infDto = (inf == null) ? null : WaaSObjectConverter.convertToInfractionDto(inf);
            Resource<Infraction> res = new Resource<>();
            res.setEntity(infDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    //===============metrics starts here=============
    /**
     * Submits externally collected metric data.
     *
     * @param req The HTTP request.
     * @param metrics The metric DTOs to submit.
     *
     * @return The metric dtos.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/user/{uname}/metirc")
    @Description("Submits externally collected metric data.")
    public Map<String, Object> createMetrics(@Context HttpServletRequest req,
            @PathParam("pid") final BigInteger policyId,
            @PathParam("uname") final String userName,
            Map<Long, Double> metricDtos) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.",
                    Status.BAD_REQUEST);
        }

        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User Name cannot be null or an empty string.",
                    Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying metrics!", Status.BAD_REQUEST);
        }

        PrincipalUser existingUser = userService.findUserByUsername(userName);
        if (existingUser == null) {
            throw new WebApplicationException("User doesn't exist for querying metrics!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query anything under this user!", Status.BAD_REQUEST);
        } else if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have privilege to query metrics for this policy!", Status.BAD_REQUEST);
        }

        if (existingUser != null && !existingPolicy.getUsers().contains(existingUser.getUserName())) {
            throw new WebApplicationException("Query user doesn't subject to this policy!", Status.BAD_REQUEST);
        }

        List<com.salesforce.dva.argus.entity.Metric> legalMetrics = new ArrayList<>();
        Map<Long, Double> illegalMetrics = new TreeMap<>();
        List<String> errorMessages = new ArrayList<>();

        /*        for (com.salesforce.dva.warden.dto.Metric metricDto : metricDtos) {
            try {
               com.salesforce.dva.argus.entity.Metric metric = new com.salesforce.dva.argus.entity.Metric(metricDto.getScope(), metricDto.getMetric());
               metric.setDatapoints(metricDto.getDatapoints());
                
               copyProperties(metric, metricDto);
               legalMetrics.add(metric);
            } catch (Exception e) {
                illegalMetrics.add(metricDto);
                errorMessages.add(e.getMessage());
            }
        }*/
        waaSService.creatMetrics(remoteUser, legalMetrics);

        Map<String, Object> result = new HashMap<>();

        result.put("Success", legalMetrics.size() + " metrics");
        result.put("Error", illegalMetrics.size() + " metrics");
        result.put("Error Messages", errorMessages);
        return result;
    }

    /**
     * Returns all the metrics for a specific user with user name and policy.
     *
     * @param req The HTTP request.
     *
     * @param pid	The policy ID to retrieve infractions
     * @param uname The user name to retrieve infrations
     *
     * @return The metric list.
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{pid}/user/{uname}/metric")
    @Description("Returns the metric for this user and policy.")
    public Map<Long, Double> getMetricsForPolicyAndUser(@Context HttpServletRequest req,
            @PathParam("pid") final BigInteger policyId,
            @PathParam("uname") final String userName) {
        return this.getMetricsForUserAndPolicy(req, userName, policyId);
    }

    /**
     * Returns all the metrics for a specific user with user name and policy.
     *
     * @param req The HTTP request.
     * @param uname The user name to retrieve infrations
     * @param pid	The policy ID to retrieve infractions
     *
     * @return The metric list.
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user/{uname}/policy/{pid}/metric")
    @Description("Returns the metric for this user and policy.")
    public Map<Long, Double> getMetricsForUserAndPolicy(@Context HttpServletRequest req,
            @PathParam("uname") final String userName,
            @PathParam("pid") final BigInteger policyId) {
        if (policyId == null || policyId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Policy Id cannot be null and must be a positive non-zero number.",
                    Status.BAD_REQUEST);
        }

        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User name cannot be null and must be a positive non-zero number.",
                    Status.BAD_REQUEST);
        }

        com.salesforce.dva.argus.entity.Policy existingPolicy = waaSService.getPolicy(policyId);
        if (existingPolicy == null) {
            throw new WebApplicationException("Policy doesn't exist for querying metrics!", Status.BAD_REQUEST);
        }

        PrincipalUser existingUser = userService.findUserByUsername(userName);
        if (existingUser == null) {
            throw new WebApplicationException("User doesn't exist for querying metrics!", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query anything under this user!", Status.BAD_REQUEST);
        } else if (!remoteUser.isPrivileged() && !existingPolicy.getOwners().contains(remoteUser.getUserName())) {
            throw new WebApplicationException("Remote user doesn't have privilege to query metrics for this policy!", Status.BAD_REQUEST);
        }

        if (existingUser != null && !existingPolicy.getUsers().contains(existingUser.getUserName())) {
            throw new WebApplicationException("Query user doesn't subject to this policy!", Status.BAD_REQUEST);
        }

        Map<Long, Double> result = new TreeMap<>();
        List<com.salesforce.dva.argus.entity.Metric> metrics = waaSService.getMetrics(existingPolicy, existingUser, null, null);

        if (metrics != null && !metrics.isEmpty()) {
//			result.addAll(WaaSObjectConverter.convertToMetricDtos(metrics));
        }

        return result;
    }

    //===============suspensions starts here=============
    /**
     * Returns all the suspensions for a specific user based on user name.
     *
     * @param req The HTTP request.
     * @param uname The user name to retrieve policies
     *
     * @return The suspension list.
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user/{uname}/suspension")
    @Description("Returns the infractions for this user.")
    public List<Resource<Infraction>> getSuspensionsForUser(@Context HttpServletRequest req,
            @PathParam("uname") final String userName) {

        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }
        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query suspensions with this user id!", Status.BAD_REQUEST);
        }

        PrincipalUser user = userService.findUserByUsername(userName);
        if (user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        List<Resource<Infraction>> result = new ArrayList<Resource<Infraction>>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = waaSService.getInfractionsByUser(user);

        if (infractions != null && !infractions.isEmpty()) {
            infractions = infractions.stream()
                    .filter(i -> (i.getExpirationTimestamp() != null) && (!i.getExpirationTimestamp().equals(0L)))
                    .collect(Collectors.toList());
        }
        if (infractions == null || infractions.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        for (com.salesforce.dva.argus.entity.Infraction inf : infractions) {
            EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub.path(inf.getId().toString()).build();

            int statusCode = (inf == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
            String message = (inf == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

            meta.put(MetaKey.href, userUri.toString());
            meta.put(MetaKey.status, Integer.toString(statusCode));
            meta.put(MetaKey.verb, req.getMethod());
            meta.put(MetaKey.message, message);
            meta.put(MetaKey.uiMessage, message);
            meta.put(MetaKey.devMessage, message);

            Infraction infDto = (inf == null) ? null : WaaSObjectConverter.convertToInfractionDto(inf);
            Resource<Infraction> res = new Resource<>();
            res.setEntity(infDto);
            res.setMeta(meta);
            result.add(res);
        }

        return result;
    }

    /**
     * Returns one suspension for a specific user based on user name and suspension id.
     *
     * @param req The HTTP request.
     * @param uname The user name to retrieve suspension
     * @param sid	The suspension ID to retrieve suspension
     *
     * @return The suspension .
     *
     * @throws WebApplicationException If an error occurs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user/{uname}/suspension/{sid}")
    @Description("Returns the suspension for this user and suspension id.")
    public Resource<Infraction> getSuspensionForUser(@Context HttpServletRequest req,
            @PathParam("uname") final String userName,
            @PathParam("sid") final BigInteger suspensionId) {

        if (suspensionId == null || suspensionId.compareTo(BigInteger.ZERO) < 1) {
            throw new WebApplicationException("Suspension Id cannot be null and must be a positive non-zero number.", Status.BAD_REQUEST);
        }

        if (userName == null || userName.isEmpty()) {
            throw new WebApplicationException("User name cannot be null or an empty string.", Status.BAD_REQUEST);
        }
        PrincipalUser remoteUser = getRemoteUser(req);
        if (!remoteUser.isPrivileged() && !remoteUser.getUserName().equals(userName)) {
            throw new WebApplicationException("Remote user doesn't have priveilege to query policies with this user id!", Status.BAD_REQUEST);
        }

        PrincipalUser user = userService.findUserByUsername(userName);
        if (user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        Resource<Infraction> result = null;
        com.salesforce.dva.argus.entity.Infraction suspension = null;
        List<com.salesforce.dva.argus.entity.Infraction> infractions = waaSService.getInfractionsByUser(user);

        // filter infractions based on expiration time if it is set a value
        if (infractions != null && !infractions.isEmpty()) {
            infractions = infractions.stream()
                    .filter(i -> (i.getExpirationTimestamp() != null) && (!i.getExpirationTimestamp().equals(0L)))
                    .collect(Collectors.toList());
        }

        if (infractions == null || infractions.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getReasonPhrase(), Response.Status.NOT_FOUND);
        }

        if (infractions != null && !infractions.isEmpty()) {
            suspension = infractions.stream().filter(i -> i.getId().equals(suspensionId)).collect(Collectors.toList()).get(0);
        }

        if (suspension == null) {
            throw new WebApplicationException("This user doesn't has this suspension.", Status.BAD_REQUEST);
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = ub.build();

        int statusCode = (suspension == null) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.OK.getStatusCode();
        String message = (suspension == null) ? Response.Status.NOT_FOUND.getReasonPhrase() : Response.Status.OK.getReasonPhrase();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        Infraction infDto = (suspension == null) ? null : WaaSObjectConverter.convertToInfractionDto(suspension);
        result = new Resource<Infraction>();
        result.setEntity(infDto);
        result.setMeta(meta);

        return result;
    }

    /**
     * Creates a new subscription.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param subscription The subscription object. Cannot be null.
     *
     * @return Created subscription object.
     *
     * @throws WebApplicationException The exception with 400 status will be thrown if the subscription object is null.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/subscription")
    @Description("Creates a new subscription.")
    public Resource<Subscription> createSubscription(@Context HttpServletRequest req, Subscription subscription) {
        if (subscription == null) {
            throw new WebApplicationException("Null subscription object cannot be created.", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);

        com.salesforce.dva.argus.entity.Subscription subscriptionEntity = null;
        com.salesforce.dva.argus.entity.Subscription entity = new com.salesforce.dva.argus.entity.Subscription(
                remoteUser, subscription.getHostname(), subscription.getPort());
        try {
            subscriptionEntity = waaSService.updateSubscription(entity);
        } catch (Exception e) {
            subscriptionEntity = null;
        }

        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = (subscriptionEntity == null) ? ub.build()
                : ub.path(subscriptionEntity.getId().toString()).build();

        int statusCode = (subscriptionEntity == null) ? Response.Status.BAD_REQUEST.getStatusCode()
                : Response.Status.OK.getStatusCode();
        String message = (subscriptionEntity == null) ? Response.Status.BAD_REQUEST.getReasonPhrase()
                : Response.Status.OK.getReasonPhrase();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        Subscription subscriptionDto = (subscriptionEntity == null) ? null
                : WaaSObjectConverter.convertToSubscriptionDto(Subscription.class, subscriptionEntity);

        Resource<Subscription> result = new Resource<>();
        result.setEntity(subscriptionDto);
        result.setMeta(meta);

        return result;
    }

    /**
     * Deletes a client subscription.
     *
     * @param req The HttpServlet request object. Cannot be null.
     *
     * @return REST response indicating whether the subscription deletion was successful.
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if a client subscription does not exist.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/unsubscription")
    @Description("Deletes selected subscription associated with this client.")
    public Resource<Subscription> deleteSubscription(@Context HttpServletRequest req,
            @QueryParam("id") BigInteger subscriptionId) {
        if (subscriptionId == null) {
            throw new WebApplicationException("Subscription id is needed for deletion.", Status.BAD_REQUEST);
        }

        PrincipalUser remoteUser = getRemoteUser(req);

        int statusCode = Response.Status.NOT_FOUND.getStatusCode();
        String message = Response.Status.NOT_FOUND.getReasonPhrase();

        com.salesforce.dva.argus.entity.Subscription delSubscription = waaSService.getSubscription(subscriptionId);
        if (delSubscription != null) {

            waaSService.deleteSubscription(delSubscription);

            statusCode = Response.Status.OK.getStatusCode();
            message = Response.Status.OK.getReasonPhrase();

        }
        EnumMap<MetaKey, String> meta = new EnumMap<MetaKey, String>(MetaKey.class);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI userUri = (delSubscription == null) ? ub.build() : ub.path(subscriptionId.toString()).build();

        meta.put(MetaKey.href, userUri.toString());
        meta.put(MetaKey.status, Integer.toString(statusCode));
        meta.put(MetaKey.verb, req.getMethod());
        meta.put(MetaKey.message, message);
        meta.put(MetaKey.uiMessage, message);
        meta.put(MetaKey.devMessage, message);

        Subscription subscriptionDto = delSubscription == null ? null
                : WaaSObjectConverter.convertToSubscriptionDto(Subscription.class, delSubscription);

        Resource<Subscription> result = new Resource<>();
        result.setEntity(subscriptionDto);
        result.setMeta(meta);

        return result;
    }

}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
