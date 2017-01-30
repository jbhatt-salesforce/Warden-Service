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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.OK;
import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.warden.dto.Infraction;
import com.salesforce.dva.warden.dto.Metric;
import com.salesforce.dva.warden.dto.Policy;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.dto.User;
import com.salesforce.dva.warden.ws.resources.AbstractResource.Description;

/**
 * Provides methods for users to query their usage and infractions.
 *
 * @author  Ruofan Zhang (rzhang@salesforce.com)
 */
@Path("/user")
@Description("Provides methods for users to query their usage and infractions.")
public class UserResource extends AbstractResource {

    /**
     * Returns the infractions for which the remote user is authorized to retrieve for a specific user based on user name and infraction ID.
     *
     * @param   req           The HTTP request. Cannot be null.
     * @param   userName      The user name for which to retrieve infractions. Cannot be null.
     * @param   infractionId  The ID of the infraction to retrieve. Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}/infraction/{infractionId}")
    @Description("Returns the specified infraction for this user.")
    public List<Resource<Infraction>> getInfractionForUser(@Context HttpServletRequest req, @PathParam("userName") final String userName,
                                                           @PathParam("infractionId") BigInteger infractionId) {
        requireThat(infractionId != null, "The infraction ID cannot be null.");

        List<Resource<Infraction>> result = new ArrayList<>();

        for (Resource<Infraction> resource : getInfractionsForUser(req, userName)) {
            if (resource.getEntity().getId().equals(infractionId)) {
                result.add(resource);
            }
        }

        return result;
    }

    /**
     * Returns all the infractions for which the remote user is authorized to retrieve for a specific user based on user name.
     *
     * @param   req       The HTTP request. Cannot be null.
     * @param   userName  The user name for which to retrieve infractions. Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}/infraction")
    @Description("Returns the infractions for this user.")
    public List<Resource<Infraction>> getInfractionsForUser(@Context HttpServletRequest req, @PathParam("userName") final String userName) {
        requireThat((userName != null) &&!userName.isEmpty(), "User name cannot be null or an empty string.");

        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUserName = remoteUser.getUserName();
        List<Resource<Infraction>> result = new ArrayList<>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = new ArrayList<>();
        PrincipalUser user = userService.findUserByUsername(userName);

        requireThat(user != null, "User was not found.", Status.NOT_FOUND);

        for (com.salesforce.dva.argus.entity.Infraction infraction : waaSService.getInfractionsByUserName(user.getUserName())) {
            if (remoteUser.isPrivileged() || userName.equals(remoteUserName)) {
                infractions.add(infraction);
            }
        }

        for (com.salesforce.dva.argus.entity.Infraction infraction : infractions) {
            String message, uiMessage, devMessage;

            if (userName.equals(remoteUserName)) {
                message = uiMessage = devMessage = "This infraction was incurred by you.";
            } else {
                message = uiMessage = devMessage = "This infraction was incurred by the specified user.";
            }

            URI userUri = uriInfo.getAbsolutePathBuilder().path(infraction.getId().toString()).build();
            Resource<Infraction> res = new Resource<>();

            res.setEntity(fromEntity(infraction));
            res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
            result.add(res);
        }

        return result;
    }

    /**
     * Returns all the infractions for a specific user with user name and policy for which the remote user is authorized to retrieve.
     *
     * @param   req       The HTTP request. Cannot be null.
     * @param   userName  The user name for which infractions will be retrieved. Cannot be null.
     * @param   policyId  The policy ID for which infractions will be retrieved. Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}/policy/{policyId}/infraction")
    @Description("Returns the users infractions for the given policy.")
    public List<Resource<Infraction>> getInfractionsForUserAndPolicy(@Context HttpServletRequest req, @PathParam("userName") final String userName,
                                                                     @PathParam("policyId") final BigInteger policyId) {
        requireThat((policyId != null) && (policyId.signum() >= 0), "Policy Id cannot be null and must be a positive non-zero number.");
        requireThat((userName != null) &&!userName.isEmpty(), "User name cannot be null or an empty string.");

        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUserName = remoteUser.getUserName();
        List<Resource<Infraction>> result = new ArrayList<>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = new ArrayList<>();
        com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(policyId);

        requireThat(policy != null, "Policy not found.", Status.NOT_FOUND);

        for (com.salesforce.dva.argus.entity.Infraction infraction : waaSService.getInfractionsByPolicyAndUserName(policy.getId(), userName)) {
            if (remoteUser.isPrivileged() || userName.equals(remoteUserName)) {
                infractions.add(infraction);
            }
        }

        for (com.salesforce.dva.argus.entity.Infraction infraction : infractions) {
            String message, uiMessage, devMessage;

            if (userName.equals(remoteUserName)) {
                message = uiMessage = devMessage = "This infraction was incurred by you.";
            } else {
                message = uiMessage = devMessage = "This infraction was incurred by the specified user.";
            }

            URI userUri = uriInfo.getAbsolutePathBuilder().path(infraction.getId().toString()).build();
            Resource<Infraction> res = new Resource<>();

            res.setEntity(fromEntity(infraction));
            res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
            result.add(res);
        }

        return result;
    }

    /**
     * Returns all the metrics for which the remote user is authorized to retrieve for a specific user with user name and policy. If the start time is
     * not specified it will default to -30d. If the end time is not specified, it will default to current time.
     *
     * @param   req       The HTTP request. Cannot be null.
     * @param   userName  The user name for which to retrieve infractions. Cannot be null.
     * @param   policyId  The policy ID for which to retrieve infractions. Cannot be null.
     * @param   start     An optional start date in relative form (-24h) or absolute timestamp. Must occur before the end date if specified.
     * @param   end       An optional end date in relative form (-24h) or absolute timestamp. Must occur after the start date if specified.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}/policy/{policyId}/metric")
    @Description("Returns the metric for this user and policy.")
    public List<Resource<Metric>> getMetricsForUserAndPolicy(@Context HttpServletRequest req, @PathParam("userName") final String userName,
                                                             @PathParam("policyId") final BigInteger policyId, @QueryParam("start") String start,
                                                             @QueryParam("end") String end) {
        requireThat((policyId != null) && (policyId.signum() >= 0), "Policy Id cannot be null and must be a positive non-zero number.");
        requireThat((userName != null) &&!userName.isEmpty(), "User name cannot be null or an empty string.");

        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUserName = remoteUser.getUserName();
        PrincipalUser user = userService.findUserByUsername(userName);

        requireThat(user != null, "User was not found.", Status.NOT_FOUND);

        com.salesforce.dva.argus.entity.Policy policy = waaSService.getPolicy(policyId);

        requireThat(policy != null, "Policy not found.", Status.NOT_FOUND);

        List<Resource<Metric>> result = new ArrayList<>();
        URI userUri = uriInfo.getRequestUri();
        String message, uiMessage, devMessage;

        if (remoteUser.isPrivileged() || userName.equals(remoteUserName)) {
            List<com.salesforce.dva.argus.entity.Metric> metrics = waaSService.getMetrics(policy.getId(), user.getUserName(), start, end);

            for (com.salesforce.dva.argus.entity.Metric metric : metrics) {
                if (userName.equals(remoteUserName)) {
                    message = uiMessage = devMessage = "This usage metric data was incurred by you.";
                } else {
                    message = uiMessage = devMessage = "This usage metric data was incurred by the specified user.";
                }

                Resource<Metric> res = new Resource<>();

                res.setEntity(fromEntity(metric, userName, policyId));
                res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
                result.add(res);
            }
        } else {
            message = uiMessage = devMessage = "You are not authorized to view the metric data for this user.";

            Resource<Metric> res = new Resource<>();

            res.setMeta(createMetadata(userUri, Status.FORBIDDEN.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
            result.add(res);
        }

        return result;
    }

    /**
     * Returns all the policies for a specific user based on user name for which the remote user is authorized to retrieve.
     *
     * @param   req       The HTTP request. Cannot be null.
     * @param   userName  The user name for which to retrieve policies. Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}/policy")
    @Description("Returns the policies that apply to this user.")
    public List<Resource<Policy>> getPoliciesForUser(@Context HttpServletRequest req, @PathParam("userName") final String userName) {
        requireThat((userName != null) &&!userName.isEmpty(), "User name cannot be null or an empty string.");

        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUserName = remoteUser.getUserName();
        List<Resource<Policy>> result = new ArrayList<>();
        List<com.salesforce.dva.argus.entity.Policy> policies = new ArrayList<>();

        for (com.salesforce.dva.argus.entity.Policy policy : waaSService.getPoliciesForUserName(userName)) {
            if (remoteUser.isPrivileged() || userName.equals(remoteUserName)) {
                if (!remoteUser.isPrivileged()) {
                    policy.setUsers(Arrays.asList(new String[] { userName }));
                }

                policies.add(policy);
            }
        }

        for (com.salesforce.dva.argus.entity.Policy policy : policies) {
            String message, uiMessage, devMessage;

            if (userName.equals(remoteUserName)) {
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
     * Returns one suspension for a specific user based on user name and suspension ID if the remote user is authorized to retrieve it.
     *
     * @param   req           The HTTP request. Cannot be null.
     * @param   userName      The userName for which to retrieve the suspension. Cannot be null.
     * @param   suspensionId  The suspension ID for which to retrieve the suspension. Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}/suspension/{suspensionId}")
    @Description("Returns the suspension for this user and suspension id.")
    public List<Resource<Infraction>> getSuspensionForUser(@Context HttpServletRequest req, @PathParam("userName") final String userName,
                                                           @PathParam("suspensionId") final BigInteger suspensionId) {
        List<Resource<Infraction>> result = new ArrayList<>();

        for (Resource<Infraction> resource : getSuspensionsForUser(req, userName)) {
            if (resource.getEntity().getId().equals(suspensionId)) {
                result.add(resource);
            }
        }

        return result;
    }

    /**
     * Returns all the suspensions for which the remote user is authorized to retrieve for a specific user based on user name.
     *
     * @param   req       The HTTP request. Cannot be null.
     * @param   userName  The userName for which to retrieve suspensions. Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}/suspension")
    @Description("Returns the suspensions for this user.")
    public List<Resource<Infraction>> getSuspensionsForUser(@Context HttpServletRequest req, @PathParam("userName") final String userName) {
        requireThat((userName != null) &&!userName.isEmpty(), "User name cannot be null or an empty string.");

        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUserName = remoteUser.getUserName();
        List<Resource<Infraction>> result = new ArrayList<>();
        List<com.salesforce.dva.argus.entity.Infraction> infractions = new ArrayList<>();
        PrincipalUser user = userService.findUserByUsername(userName);

        requireThat(user != null, "User was not found.", Status.NOT_FOUND);

        for (com.salesforce.dva.argus.entity.Infraction infraction : waaSService.getSuspensionsByUserName(user.getUserName())) {
            if (remoteUser.isPrivileged() || userName.equals(remoteUserName)) {
                infractions.add(infraction);
            }
        }

        for (com.salesforce.dva.argus.entity.Infraction infraction : infractions) {
            String message, uiMessage, devMessage;

            if (userName.equals(remoteUserName)) {
                message = uiMessage = devMessage = "This suspension was incurred by you.";
            } else {
                message = uiMessage = devMessage = "This suspension was incurred by the specified user.";
            }

            URI userUri = uriInfo.getAbsolutePathBuilder().path(infraction.getId().toString()).build();
            Resource<Infraction> res = new Resource<>();

            res.setEntity(fromEntity(infraction));
            res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
            result.add(res);
        }

        return result;
    }

    /**
     * Returns the user having the given user name if the remote user is authorized to retrieve it.
     *
     * @param   req       The HTTP request. Cannot be null.
     * @param   userName  The user name to retrieve. Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}")
    @Description("Returns the user having the given ID.")
    public List<Resource<User>> getUserByUserName(@Context HttpServletRequest req, @PathParam("userName") final String userName) {
        requireThat(userName != null, "The userName cannot be null.");

        return getUsers(req, userName);
    }

    /**
     * Returns all users for which the remote user is authorized to retrieve.
     *
     * @param   req       The HTTP request. Cannot be null.
     * @param   userName  An optional userName to filter on. May be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Description("Returns the requested users.")
    public List<Resource<User>> getUsers(@Context HttpServletRequest req, @QueryParam("userName") String userName) {
        PrincipalUser remoteUser = getRemoteUser(req);
        String remoteUserName = remoteUser.getUserName();
        List<Resource<User>> result = new ArrayList<>();
        List<PrincipalUser> users = new ArrayList<>();
        BigInteger id;

        try {
            id = new BigInteger(userName);
        } catch (NumberFormatException | NullPointerException ex) {
            id = null;
        }

        for (PrincipalUser user : userService.getPrincipalUsers()) {
            if ((remoteUser.isPrivileged() || user.getUserName().equals(remoteUserName))
                    && ((userName == null) || userName.equals(user.getUserName()) || user.getId().equals(id))) {
                users.add(user);
            }
        }

        for (PrincipalUser user : users) {
            String message, uiMessage, devMessage;

            if (user.getUserName().equals(remoteUserName)) {
                message = "This is your user information.";
                uiMessage = "This is the information associated with your user.";
                devMessage = "This data represents your identity.  It is always visible to you and any privileged user.";
            } else {
                message = "This is not your user information, but you are authorized to view it.";
                uiMessage = "This is the information associated with another user which you are authorized to view.";
                devMessage = "This data doesn't represent your identity.  It is only visible as a result of your privileged user status.";
            }

            URI userUri = uriInfo.getBaseUriBuilder().path("user").path(user.getId().toString()).build();
            Resource<User> res = new Resource<>();

            res.setEntity(fromEntity(user));
            res.setMeta(createMetadata(userUri, OK.getStatusCode(), req.getMethod(), message, uiMessage, devMessage));
            result.add(res);
        }

        return result;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



