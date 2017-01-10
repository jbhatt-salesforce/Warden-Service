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
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Metric;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.SuspensionLevel;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.ws.resources.AbstractResource.Description;

import java.math.BigInteger;
import java.net.URI;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.TimeZone;
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
import javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Warden web service resources.
 *
 * @author Ruofan Zhang (rzhang@salesforce.com)
 */
@Path("/policy")
@Description("Provides methods to manipulate warden entities.")
public class PolicyResource extends AbstractResource {

    /**
     * Returns the list of policies for which the remote user is the creator or an owner. If a username is specified, the results are filtered to
     * display only policies to which the specified username is subject to.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param username The optional user name used for filtering the policies.
     * @param policyId The optional policy ID to retrieve.
     * @param service The optional service name.
     * @param name The optional policy name.
     *
     * @return The resulting list of resources.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Description("Returns all policies.")
    public List<Resource<Policy>> getPolicies(@Context HttpServletRequest req,
            @QueryParam("username") String username, @QueryParam("pid") BigInteger policyId, @QueryParam("service") String service, @QueryParam("name") String name) {

        requireThat(username == null || !username.isEmpty(), "If specified, user name cannot be an empty string.");
        requireThat(service == null || !service.isEmpty(), "If specified, service name cannot be an empty string.");
        requireThat(name == null || !name.isEmpty(), "If specified, policy name cannot be an empty string.");
        requireThat(policyId == null || policyId.signum() >= 0, "If specified the policy ID must be a positive integer.");

        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUsername = remoteUser.getUserName();
        List<Resource<Policy>> result = new ArrayList<>();
        List<com.salesforce.dva.argus.entity.Policy> policies = new ArrayList<>();

        for (com.salesforce.dva.argus.entity.Policy policy : waaSService.getPolicies()) {
            if ((remoteUser.isPrivileged() || policy.getCreatedBy().getUserName().equals(remoteUsername)
                    || policy.getOwners().contains(remoteUsername))) {
                if ((username == null || policy.getUsers().contains(username)) && (policyId == null || policy.getId().equals(policyId)) && (service
                        == null || policy.getService().equals(service)) && (name == null || policy.getName().equals(name))) {
                    policies.add(policy);
                }
            }
        }
        for (com.salesforce.dva.argus.entity.Policy policy : policies) {
            String message, uiMessage, devMessage;

            if (policy.getUsers().contains(remoteUsername)) {
                message = uiMessage = devMessage = "You are subject to this policy.";
            } else {
                message = uiMessage = devMessage = "The specified user is subject to this policy.";
            }

            URI userUri = uriInfo.getAbsolutePathBuilder().path(policy.getId().toString()).build();
            Resource<Policy> res = new Resource<>();

            res.setEntity(fromEntity(policy));
            res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
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
     * @return The resulting list of resources.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Description("Creates policies.")
    public List<Resource<Policy>> createPolicies(@Context HttpServletRequest req, List<Policy> policies) {
        requireThat(policies != null, "The list of policies to create cannot be null.");
        List<Resource<Policy>> result = new ArrayList<>();
        PrincipalUser remoteUser = getRemoteUser(req);
        for (Policy policy : policies) {
            Resource<Policy> resource = new Resource<>();
            String message, devMessage, uiMessage;
            int status;
            Policy entity = null;
            URI uri;
            if (policy.getId() != null) {
                message = "The ID must be null when creating a new policy.";
                uiMessage = "Creating a policy with an explicit ID is not supported.";
                devMessage = "The policy being created has a non-null ID.  The ID field is automatically generated and should specified as null.";
                status = Status.BAD_REQUEST.getStatusCode();
                uri = uriInfo.getRequestUri();
            } else if (waaSService.getPolicy(policy.getName(), policy.getService()) != null) {
                message = "Cannot create a duplicate policy.";
                uiMessage = "A policy already exists for this name and service combination.";
                devMessage = "Creating a policy with for this sevice and name combination would result in a duplicate record.";
                status = Status.BAD_REQUEST.getStatusCode();
                uri = uriInfo.getRequestUri();
            } else {
                try {
                    com.salesforce.dva.argus.entity.Policy toCreate = toEntity(policy);
                    toCreate.setCreatedBy(remoteUser);
                    toCreate.setModifiedBy(remoteUser);
                    message = uiMessage = devMessage = "Successfully created policy.";
                    entity = fromEntity(waaSService.updatePolicy(toCreate));
                    status = Status.CREATED.getStatusCode();
                    uri = uriInfo.getAbsolutePathBuilder().path(entity.getId().toString()).build();
                } catch (Exception ex) {
                    message = "Failed to create policy.";
                    uiMessage = "An error occurred creating the policy.";
                    devMessage = ex.getLocalizedMessage();
                    status = Status.BAD_REQUEST.getStatusCode();
                    uri = uriInfo.getRequestUri();
                }
            }
            resource.setEntity(entity);
            resource.setMeta(createMetadata(uri, status, req.getMethod(), message, uiMessage, devMessage));
            result.add(resource);
        }
        return result;
    }

    /**
     * Deletes policies.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyIds The IDs of the policies to delete. Cannot be null, but may be empty.
     *
     * @return The resulting list of resources.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Description("Deletes selected policies owned by this user.")
    public List<Resource<Policy>> deletePolicies(@Context HttpServletRequest req, @QueryParam("id") List<BigInteger> policyIds) {
        requireThat(policyIds != null, "The list of policies to delete cannot be null.");
        List<Resource<Policy>> result = new ArrayList<>();
        PrincipalUser remoteUser = getRemoteUser(req);
        for (BigInteger id : policyIds) {
            Resource<Policy> resource = new Resource<>();
            String message, devMessage, uiMessage;
            int status;
            Policy entity = null;
            URI uri;
            com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(id);
            uri = uriInfo.getAbsolutePathBuilder().path(id.toString()).build();
            if (policy == null) {
                message = uiMessage = devMessage = "The specified policy doesn't exist.";
                status = Status.NOT_FOUND.getStatusCode();
                uri = uriInfo.getAbsolutePathBuilder().path(id.toString()).build();
            } else if ((!policy.getCreatedBy().equals(remoteUser) && !policy.getOwners().contains(remoteUser.getUserName()))
                    && !remoteUser.isPrivileged()) {
                message = uiMessage = devMessage = "You are not authorized to delete this policy.";
                status = Status.UNAUTHORIZED.getStatusCode();
            } else {
                try {
                    message = uiMessage = devMessage = "Successfully deleted policy.";
                    entity = fromEntity(policy);
                    waaSService.deletePolicy(policy);
                    status = Status.OK.getStatusCode();
                } catch (Exception ex) {
                    message = "Failed to delete policy.";
                    uiMessage = "An error occurred delete the policy.";
                    devMessage = ex.getLocalizedMessage();
                    status = Status.BAD_REQUEST.getStatusCode();
                }
            }
            resource.setEntity(entity);
            resource.setMeta(createMetadata(uri, status, req.getMethod(), message, uiMessage, devMessage));
            result.add(resource);
        }
        return result;
    }

    /**
     * Updates existing policies.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policies Policies objects. Cannot be null.
     *
     * @return The resulting list of resources.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Description("Updates policy objects")
    public List<Resource<Policy>> updatePolicies(@Context HttpServletRequest req, List<Policy> policies) {
        requireThat(policies != null, "The list of policies to create cannot be null.");
        List<Resource<Policy>> result = new ArrayList<>();
        PrincipalUser remoteUser = getRemoteUser(req);
        for (Policy policy : policies) {
            Resource<Policy> resource = new Resource<>();
            String message, devMessage, uiMessage;
            int status;
            Policy entity = null;
            URI uri;
            if (policy.getId() == null) {
                message = "The ID must not be null when updating an existing policy.";
                uiMessage = "Updating a policy without an ID is not supported.";
                devMessage = "The policy being updated has a null ID.  The ID field is required when updating an existing policy.";
                status = Status.BAD_REQUEST.getStatusCode();
                uri = uriInfo.getRequestUri();
            } else {
                com.salesforce.dva.argus.entity.Policy existing = waaSService.getPolicy(policy.getId());
                if (existing != null && !remoteUser.isPrivileged() && !existing.getCreatedBy().equals(remoteUser)
                        && !existing.getOwners().contains(remoteUser.getUserName())) {
                    message = uiMessage = devMessage = "You are not authorized to update this policy.";
                    status = Status.UNAUTHORIZED.getStatusCode();
                    uri = uriInfo.getAbsolutePathBuilder().path(existing.getId().toString()).build();
                } else {
                    try {
                        com.salesforce.dva.argus.entity.Policy toUpdate = toEntity(policy);
                        toUpdate.setCreatedBy(existing == null ? remoteUser : existing.getCreatedBy());
                        toUpdate.setModifiedBy(remoteUser);
                        message = uiMessage = devMessage = existing == null ? "Successfully created policy." : "Successfully updated policy.";
                        entity = fromEntity(waaSService.updatePolicy(toUpdate));
                        status = existing == null ? Status.CREATED.getStatusCode() : Status.OK.getStatusCode();
                        uri = uriInfo.getAbsolutePathBuilder().path(entity.getId().toString()).build();
                    } catch (Exception ex) {
                        message = "Failed to update policy.";
                        uiMessage = "An error occurred updating the policy.";
                        devMessage = ex.getLocalizedMessage();
                        status = Status.BAD_REQUEST.getStatusCode();
                        uri = existing == null ? uriInfo.getRequestUri() : uriInfo.getAbsolutePathBuilder().path(existing.getId().toString()).build();
                    }
                }
            }
            resource.setEntity(entity);
            resource.setMeta(createMetadata(uri, status, req.getMethod(), message, uiMessage, devMessage));
            result.add(resource);
        }
        return result;
    }

    /**
     * Finds a policy by policy ID.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid ID of a policy. Cannot be null and must be a positive non-zero number.
     *
     * @return The resulting list of resources.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}")
    @Description("Returns a policy by its ID.")
    public List<Resource<Policy>> getPolicyById(@Context HttpServletRequest req, @PathParam("pid") BigInteger pid) {
        requireThat(pid != null, "The policy ID cannot be null.");
        return getPolicies(req, null, pid, null, null);
    }

    /**
     * Deletes the policy.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid The policy ID. Cannot be null and must be a positive non-zero number.
     *
     * @return The resulting list of resources.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}")
    @Description("Deletes the policy having the given ID.")
    public List<Resource<Policy>> deletePolicy(@Context HttpServletRequest req, @PathParam("pid") BigInteger pid) {
        requireThat(pid != null && pid.signum() >= 0, "Policy ID must be a positive integer.");
        return deletePolicies(req, Arrays.asList(new BigInteger[]{pid}));
    }

    /**
     * Updates existing policy.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param pid The id of an policy. Cannot be null.
     * @param policy The new policy object. Cannot be null.
     *
     * @return The resulting list of resources.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{pid}")
    @Description("Updates an policy having the given ID.")
    public List<Resource<Policy>> updatePolicy(@Context HttpServletRequest req, @PathParam("pid") BigInteger pid, Policy policy) {
        requireThat(pid != null && pid.signum() >= 0, "Policy ID must be a positive integer.");
        requireThat(policy != null, "The policy data cannot be null.");
        policy.setId(pid);
        return updatePolicies(req, Arrays.asList(new Policy[]{policy}));
    }

    /**
     * Returns the list of suspension levels owned by the owner with given policy id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     * @param levelId The optional level ID.
     *
     * @return The resulting list of resources.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/level")
    @Description("Returns all levels with policy id.")
    public List<Resource<SuspensionLevel>> getLevels(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @QueryParam("lid") BigInteger levelId) {
        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUsername = remoteUser.getUserName();
        List<Resource<SuspensionLevel>> result = new ArrayList<>();
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        requireThat(levelId == null || levelId.signum() >= 0, "The level ID if specified must be a positive integer.");
        com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(policyId);
        requireThat(policy != null, "The specified policy doesn't exist.", Status.NOT_FOUND);
        requireThat((remoteUser.isPrivileged() || policy.getCreatedBy().getUserName().equals(remoteUsername)
                || policy.getOwners().contains(remoteUsername)), "You are not authorized to view the levels for this policy.", Status.UNAUTHORIZED);
        List<com.salesforce.dva.argus.entity.SuspensionLevel> suspensionLevels = policy.getSuspensionLevels();
        for (com.salesforce.dva.argus.entity.SuspensionLevel level : suspensionLevels) {
            if (levelId == null || level.getId().equals(levelId)) {
                String message, uiMessage, devMessage;
                message = uiMessage = devMessage = MessageFormat.format("This is level {0} out of {1}.", level.getLevelNumber(), suspensionLevels.size());

                URI userUri = uriInfo.getAbsolutePathBuilder().path(level.getId().toString()).build();
                Resource<SuspensionLevel> res = new Resource<>();

                res.setEntity(fromEntity(level));
                res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
                result.add(res);
            }
        }
        return result;
    }

    /**
     * Creates new levels for existing policy.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy id. Cannot be null.
     * @param levels	New levels to be created. Cannot be null.
     *
     * @return The resulting list of resources.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{pid}/level")
    @Description("Creates levels for a specific policy.")
    public List<Resource<SuspensionLevel>> createLevels(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            List<SuspensionLevel> levels) {
        requireThat(levels != null, "The list of levels to create cannot be null.");
        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUsername = remoteUser.getUserName();
        List<Resource<SuspensionLevel>> result = new ArrayList<>();
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(policyId);
        requireThat(policy != null, "The specified policy doesn't exist.", Status.NOT_FOUND);
        requireThat((remoteUser.isPrivileged() || policy.getCreatedBy().getUserName().equals(remoteUsername)
                || policy.getOwners().contains(remoteUsername)), "You are not authorized to create the levels for this policy.", Status.UNAUTHORIZED);
        for (SuspensionLevel level : levels) {
            Resource<SuspensionLevel> resource = new Resource<>();
            String message, devMessage, uiMessage;
            int status;
            SuspensionLevel entity = null;
            URI uri;
            if (level.getId() != null) {
                message = "The ID must be null when creating a new level.";
                uiMessage = "Creating a level with an explicit ID is not supported.";
                devMessage = "The level being created has a non-null ID.  The ID field is automatically generated and should specified as null.";
                status = Status.BAD_REQUEST.getStatusCode();
                uri = uriInfo.getRequestUri();
            } else if (policy.getSuspensionLevels().stream().anyMatch((existing) -> (existing.getLevelNumber() == level.getLevelNumber()))) {
                message = "Cannot create a duplicate level.";
                uiMessage = "A level having this level number already exists for this policy.";
                devMessage = "Creating a level having this level number for this policy would result in a duplicate record.";
                status = Status.BAD_REQUEST.getStatusCode();
                uri = uriInfo.getRequestUri();
            } else {
                try {
                    com.salesforce.dva.argus.entity.SuspensionLevel toCreate = toEntity(level);
                    toCreate.setPolicy(policy);
                    toCreate.setCreatedBy(remoteUser);
                    toCreate.setModifiedBy(remoteUser);
                    message = uiMessage = devMessage = "Successfully created level.";
                    entity = fromEntity(waaSService.updateLevel(toCreate));
                    status = Status.CREATED.getStatusCode();
                    uri = uriInfo.getAbsolutePathBuilder().path(entity.getId().toString()).build();
                } catch (Exception ex) {
                    message = "Failed to create level.";
                    uiMessage = "An error occurred creating the level.";
                    devMessage = ex.getLocalizedMessage();
                    status = Status.BAD_REQUEST.getStatusCode();
                    uri = uriInfo.getRequestUri();
                }
            }
            resource.setEntity(entity);
            resource.setMeta(createMetadata(uri, status, req.getMethod(), message, uiMessage, devMessage));
            result.add(resource);
        }
        return result;
    }

    /**
     * Deletes levels.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null and must be a positive non-zero number.
     * @param levelIds The level IDs. Cannot be null, but may be empty.
     *
     * @return The resulting list of resources.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/level")
    @Description("Deletes the specified levels for a policy.")
    public List<Resource<SuspensionLevel>> deleteLevels(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @QueryParam("id") List<BigInteger> levelIds) {
        requireThat(levelIds != null, "The list of levels to delete cannot be null.");
        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUsername = remoteUser.getUserName();
        List<Resource<SuspensionLevel>> result = new ArrayList<>();
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(policyId);
        requireThat(policy != null, "The specified policy doesn't exist.", Status.NOT_FOUND);
        requireThat((remoteUser.isPrivileged() || policy.getCreatedBy().getUserName().equals(remoteUsername)
                || policy.getOwners().contains(remoteUsername)), "You are not authorized to delete the levels for this policy.", Status.UNAUTHORIZED);
        for (BigInteger levelId : levelIds) {
            Resource<SuspensionLevel> resource = new Resource<>();
            String message, devMessage, uiMessage;
            int status;
            SuspensionLevel entity = null;
            URI uri;
            uri = uriInfo.getAbsolutePathBuilder().path(levelId.toString()).build();
            if (policy.getSuspensionLevels().stream().noneMatch((level) -> (level.getId() == levelId))) {
                message = uiMessage = devMessage = "The specified level doesn't exist for the policy.";
                status = Status.NOT_FOUND.getStatusCode();
                uri = uriInfo.getRequestUri();
            } else if ((!policy.getCreatedBy().equals(remoteUser) && !policy.getOwners().contains(remoteUser.getUserName()))
                    && !remoteUser.isPrivileged()) {
                message = uiMessage = devMessage = "You are not authorized to delete the levels for this policy.";
                status = Status.UNAUTHORIZED.getStatusCode();
            } else {
                try {
                    message = uiMessage = devMessage = "Successfully deleted level.";
                    com.salesforce.dva.argus.entity.SuspensionLevel level = waaSService.getLevel(policy, levelId);
                    entity = fromEntity(level);
                    waaSService.deleteLevel(level);
                    status = Status.OK.getStatusCode();
                } catch (Exception ex) {
                    message = "Failed to delete level.";
                    uiMessage = "An error occurred deleting the level.";
                    devMessage = ex.getLocalizedMessage();
                    status = Status.BAD_REQUEST.getStatusCode();
                }
            }
            resource.setEntity(entity);
            resource.setMeta(createMetadata(uri, status, req.getMethod(), message, uiMessage, devMessage));
            result.add(resource);
        }
        return result;
    }

    /**
     * Updates existing levels for given policy id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     * @param levels The levels to update. Cannot be null, but may be empty.
     *
     * @return The resulting list of resources.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{pid}/level")
    @Description("Updates policy objects")
    public List<Resource<SuspensionLevel>> updateLevels(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            List<SuspensionLevel> levels) {
        requireThat(levels != null, "The list of levels to update cannot be null.");
        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUsername = remoteUser.getUserName();
        List<Resource<SuspensionLevel>> result = new ArrayList<>();
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(policyId);
        requireThat(policy != null, "The specified policy doesn't exist.", Status.NOT_FOUND);
        requireThat((remoteUser.isPrivileged() || policy.getCreatedBy().getUserName().equals(remoteUsername)
                || policy.getOwners().contains(remoteUsername)), "You are not authorized to delete the levels for this policy.", Status.UNAUTHORIZED);
        for (SuspensionLevel level : levels) {
            Resource<SuspensionLevel> resource = new Resource<>();
            String message, devMessage, uiMessage;
            int status;
            SuspensionLevel entity = null;
            URI uri;

            if (policy.getSuspensionLevels().stream().noneMatch((existing) -> (level.getId() != null && existing.getId().equals(existing.getId())))) {
                message = uiMessage = devMessage = "The specified level doesn't exist for the policy.";
                status = Status.NOT_FOUND.getStatusCode();
                uri = uriInfo.getRequestUri();
            } else {
                com.salesforce.dva.argus.entity.SuspensionLevel existing = waaSService.getLevel(policy, level.getId());
                try {
                    com.salesforce.dva.argus.entity.SuspensionLevel toUpdate = toEntity(level);
                    toUpdate.setCreatedBy(existing == null ? remoteUser : existing.getCreatedBy());
                    toUpdate.setModifiedBy(remoteUser);
                    message = uiMessage = devMessage = existing == null ? "Successfully created level." : "Successfully updated level.";
                    entity = fromEntity(waaSService.updateLevel(toUpdate));
                    status = existing == null ? Status.CREATED.getStatusCode() : Status.OK.getStatusCode();
                    uri = uriInfo.getAbsolutePathBuilder().path(entity.getId().toString()).build();
                } catch (Exception ex) {
                    message = "Failed to update level.";
                    uiMessage = "An error occurred updating the level.";
                    devMessage = ex.getLocalizedMessage();
                    status = Status.BAD_REQUEST.getStatusCode();
                    uri = existing == null ? uriInfo.getRequestUri() : uriInfo.getAbsolutePathBuilder().path(existing.getId().toString()).build();
                }
            }
            resource.setEntity(entity);
            resource.setMeta(createMetadata(uri, status, req.getMethod(), message, uiMessage, devMessage));
            result.add(resource);
        }
        return result;
    }

    /**
     * Finds a suspension level by policy ID and level id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId ID of a policy. Cannot be null and must be a positive non-zero number.
     * @param levelId ID of suspension level. Cannot be null and must be a positive non-zero number.
     * @return Policy
     *
     * @throws WebApplicationException The exception with 404 status will be thrown if a policy does not exist.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/level/{lid}")
    @Description("Returns a policy by its ID.")
    public List<Resource<SuspensionLevel>> getLevel(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("lid") BigInteger levelId) {
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        requireThat(levelId != null && levelId.signum() >= 0, "The level ID must be a positive integer.");
        return getLevels(req, policyId, levelId);
    }

    /**
     * Deletes a suspension level.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId ID of a policy. Cannot be null and must be a positive non-zero number.
     * @param levelId ID of suspension level. Cannot be null and must be a positive non-zero number.
     *
     * @return The resulting list of resources.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/level/{levelid}")
    @Description("Deletes the suspension level with policy ID and level ID.")
    public List<Resource<SuspensionLevel>> deleteLevel(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("levelid") BigInteger levelId) {
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        requireThat(levelId != null && levelId.signum() >= 0, "The level ID must be a positive integer.");
        return deleteLevels(req, policyId, Arrays.asList(new BigInteger[]{levelId}));
    }

    /**
     * Updates existing suspension level with given policy id and level id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The id of an policy. Cannot be null.
     * @param levelId	The id of suspension level. Cannot be null.
     * @param level The new suspension level object. Cannot be null.
     *
     * @return The resulting list of resources.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{pid}/level/{levelid}")
    @Description("Updates an suspension level having the given policy ID and level ID.")
    public List<Resource<SuspensionLevel>> updateLevel(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("levelid") BigInteger levelId,
            SuspensionLevel level) {
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        requireThat(levelId != null && levelId.signum() >= 0, "The level ID must be a positive integer.");
        level.setId(levelId);
        return updateLevels(req, policyId, Arrays.asList(new SuspensionLevel[]{level}));
    }

    /**
     * Returns all infractions with given policy id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     * @param infractionId The optional infraction ID used for filtering the results.
     * @param username The optional user name used for filtering the results.
     *
     * @return The resulting list of resources.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/infraction")
    @Description("Returns all infractions with policy id.")
    public List<Resource<Infraction>> getInfractions(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @QueryParam("iid") BigInteger infractionId,
            @QueryParam("username") String username) {
        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUsername = remoteUser.getUserName();
        List<Resource<Infraction>> result = new ArrayList<>();
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        requireThat(infractionId == null || infractionId.signum() >= 0, "The infraction ID if specified must be a positive integer.");
        requireThat(username == null || !username.isEmpty(), "The user name if specified cannot be empty.");
        com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(policyId);
        requireThat(policy != null, "The specified policy doesn't exist.", Status.NOT_FOUND);
        requireThat((remoteUser.isPrivileged() || policy.getCreatedBy().getUserName().equals(remoteUsername)
                || policy.getOwners().contains(remoteUsername)), "You are not authorized to view the infractions for this policy.", Status.UNAUTHORIZED);
        List<com.salesforce.dva.argus.entity.Infraction> infractions = waaSService.getInfractions(policy);
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (com.salesforce.dva.argus.entity.Infraction infraction : infractions) {
            String user = infraction.getUser().getUserName();
            Long occurred = infraction.getInfractionTimestamp();
            if (username == null || infraction.getUser().getUserName().equals(username) && (infractionId == null
                    || infraction.getId().equals(infractionId))) {
                String message, uiMessage, devMessage;
                message = uiMessage = devMessage = MessageFormat.format("Incurred by {0} at {1}.", user, sdf.format(new Date(occurred)));

                URI userUri = uriInfo.getAbsolutePathBuilder().path(infraction.getId().toString()).build();
                Resource<Infraction> res = new Resource<>();

                res.setEntity(fromEntity(infraction));
                res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
                result.add(res);
            }
        }
        return result;
    }

    /**
     * Finds an infraction by policy ID and infraction id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     * @param infractionId The infraction ID. Cannot be null.
     *
     * @return The resulting list of resources.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/infraction/{iid}")
    @Description("Returns an infraction by policy ID and its ID.")
    public List<Resource<Infraction>> getInfraction(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("iid") BigInteger infractionId) {
        requireThat(policyId != null && policyId.signum() >= 0, "The policy ID must be a positive integer.");
        requireThat(infractionId == null || infractionId.signum() >= 0, "The infraction ID if specified must be a positive integer.");
        return getInfractions(req, policyId, infractionId, null);
    }

    /**
     * Returns all suspensions with given policy id.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     * @param suspensionId The optional suspension ID used for filtering the results.
     * @param username The optional user name used for filtering the results.
     *
     * @return The resulting list of resources.
     *
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/user/{uname}/suspension")
    @Description("Returns all infractions with policy id and user name if suspension happens.")
    public List<Resource<Infraction>> getSuspensionForUserAndPolicy(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @QueryParam("sid") BigInteger suspensionId,
            @PathParam("uname") String username) {
        List<Resource<Infraction>> infractions = getInfractions(req, policyId, suspensionId, username);
        return infractions.stream().filter(i -> i.getEntity() != null && i.getEntity().getExpirationTimestamp() != null && (username == null
                || username.equals(i.getEntity().getUsername()))).collect(Collectors.toList());
    }

    /**
     * Deletes suspensions for user.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     * @param suspensionIds The optional suspension ID used for filtering the operation.
     * @param username The optional user name used for filtering the results.
     *
     * @return The resulting list of resources.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/user/{uname}/suspension")
    @Description("Deletes all infractions with policy id and user id if suspension happens.")
    public List<Resource<Infraction>> deleteSuspensionForUser(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId,
            @PathParam("uname") String username,
            @QueryParam("id") List<BigInteger> suspensionIds) {
        List<Resource<Infraction>> result = new ArrayList<>();
        List<Resource<Infraction>> suspensions = getSuspensionForUserAndPolicy(req, policyId, null, username);
        for (Resource<Infraction> suspension : suspensions) {
            Resource<Infraction> res = new Resource<>();
            URI userUri = uriInfo.getAbsolutePathBuilder().path(suspension.getEntity().getId().toString()).build();
            String message, uiMessage, devMessage;
            int status;
            if(suspensionIds == null || suspensionIds.contains(suspension.getEntity().getId())){
                res.setEntity(suspension.getEntity());
                try {
                    waaSService.deleteInfraction(toEntity(suspension.getEntity()));
                    message = uiMessage = devMessage = "Suspension deleted successfully.";
                    status = OK.getStatusCode();
                } catch (Exception ex) {
                    message = uiMessage= "An error occurred deleting the suspension.";
                    devMessage = ex.getLocalizedMessage();
                    status = Status.BAD_REQUEST.getStatusCode();
                }
            } else {
                res.setEntity(null);
                message = uiMessage = devMessage = "Suspension doesn't exist for this policy.";
                status = Status.NOT_FOUND.getStatusCode();
                res.setEntity(null);
            }
            res.setMeta(createMetadata(userUri, status, req.getMethod(), message, uiMessage, devMessage));
            result.add(res);
        }
        return result;
    }

    /**
     * Returns all infractions with given policy id if suspension happens.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     * 
     * @return The resulting list of resources.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/suspension")
    @Description("Returns all infractions with policy id and user id if suspension happens.")
    public List<Resource<Infraction>> getSuspension(@Context HttpServletRequest req,
            @PathParam("pid") BigInteger policyId) {
        return getSuspensionForUserAndPolicy(req, policyId, null, null);
    }

    /**
     * Deletes suspensions for policy.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param policyId The policy ID. Cannot be null.
     * @param suspensionIds The optional suspension IDs.
     * 
     * @return The resulting list of resources.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/suspension")
    @Description("Deletes all infractions with policy id and user id if suspension happens.")
    public List<Resource<Infraction>> deleteSuspension(@Context HttpServletRequest req, @PathParam("pid") BigInteger policyId,
            @QueryParam("id") List<BigInteger> suspensionIds) {
        return deleteSuspensionForUser(req, policyId, null, suspensionIds);
    }

    /**
     * Submits externally collected metric data.
     *
     * @param req The HTTP request.
     * @param policyId The policy ID. Cannot be null.
     * @param username The optional user name used for filtering the results.
     * @param datapoints The data points to submit.
     *
     * @return The resulting list of resources.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/user/{uname}/metric")
    @Description("Submits externally collected metric data.")
    public List<Resource<Metric>> createMetrics(@Context HttpServletRequest req,
            @PathParam("pid") final BigInteger policyId,
            @PathParam("uname") final String username,
            Map<Long, Double> datapoints) {
        requireThat(policyId != null, "The policy ID cannot be null.");
        requireThat(username != null && !username.isEmpty(), "The user name cannot be null or empty.");
        requireThat(datapoints != null && !datapoints.isEmpty(), "You must supply at least one datapoint.");
        List<Resource<Policy>> policies = getPolicies(req, username, policyId, null, null);
        requireThat(policies.size()==1, "Either the policy doesn't exist, the user isn't subject to it, or you are not authorized to perform this operation.");
        LongSummaryStatistics stats = datapoints.keySet().stream().collect(Collectors.summarizingLong(Long::longValue));
        waaSService.updateMetric(policyId, username, datapoints);
        List<Resource<Metric>> result = rc.getResource(UserResource.class).getMetricsForUserAndPolicy(req, username, policyId, String.valueOf(stats.getMin()), String.valueOf(stats.getMax()));
        result.get(0).getEntity().setDatapoints(datapoints);
        return result;
    }

    /**
     * Returns all the metrics for which the remote user is authorized to retrieve for a specific user with user name and policy. If the start time is
     * not specified it will default to -30d. If the end time is not specified, it will default to current time.
     *
     * @param   req       The HTTP request. Cannot be null.
     * @param   username  The user name for which to retrieve infractions. Cannot be null.
     * @param   policyId  The policy ID for which to retrieve infractions. Cannot be null.
     * @param   start     An optional start date in relative form (-24h) or absolute timestamp. Must occur before the end date if specified.
     * @param   end       An optional end date in relative form (-24h) or absolute timestamp. Must occur after the start date if specified.
     *
     * @return  The resulting list of resources.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{pid}/user/{uname}/metric")
    @Description("Returns the metric for this user and policy.")
    public List<Resource<Metric>> getMetricsForPolicyAndUser(@Context HttpServletRequest req,
        @PathParam("username") final String username,
        @PathParam("policyId") final BigInteger policyId,
        @QueryParam("start") String start,
        @QueryParam("end") String end) {
        requireThat(policyId != null && policyId.signum() >= 0, "Policy Id cannot be null and must be a positive non-zero number.");
        requireThat(username != null && !username.isEmpty(), "User name cannot be null or an empty string.");

        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUsername = remoteUser.getUserName();
        PrincipalUser user = userService.findUserByUsername(username);

        requireThat(user != null, "User was not found.", Status.NOT_FOUND);

        com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(policyId);

        requireThat(policy != null, "Policy not found.", Status.NOT_FOUND);
        List<Resource<Metric>> result = new ArrayList<>();
        URI userUri = uriInfo.getRequestUri();
        String message, uiMessage, devMessage;
        if (remoteUser.isPrivileged() || policy.getCreatedBy().getUserName().equals(remoteUsername) || policy.getOwners().contains(remoteUsername) || username.equals(remoteUsername)) {
            List<com.salesforce.dva.argus.entity.Metric> metrics = waaSService.getMetrics(policy, user, start, end);

            for (com.salesforce.dva.argus.entity.Metric metric : metrics) {

                if (username.equals(remoteUsername)) {
                    message = uiMessage = devMessage = "This usage metric data was incurred by you.";
                } else {
                    message = uiMessage = devMessage = "This usage metric data was incurred by the specified user.";
                }

                Resource<Metric> res = new Resource<>();

                res.setEntity(fromEntity(metric, username, policyId));
                res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
                result.add(res);
            }
        } else {
            message = uiMessage = devMessage = "You are not authorized to view the metric data for this user.";
            Resource<Metric> res = new Resource<>();
            res.setMeta(createMetadata(userUri, Status.UNAUTHORIZED.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
            result.add(res);
        }
        return result;
    }
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
