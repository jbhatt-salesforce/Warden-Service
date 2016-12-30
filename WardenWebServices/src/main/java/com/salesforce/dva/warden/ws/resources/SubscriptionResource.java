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
import com.salesforce.dva.warden.dto.Subscription;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.ws.dto.Converter;
import com.salesforce.dva.warden.ws.resources.AbstractResource.Description;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import javax.ws.rs.core.UriInfo;
import jersey.repackaged.com.google.common.base.Objects;

/**
 * Provides methods to manipulate subscriptions.
 *
 * @author Ruofan Zhang (rzhang@salesforce.com)
 */
@Path("/subscription")
@Description("Provides methods to manipulate subscriptions.")
public class SubscriptionResource extends AbstractResource {

    static Subscription fromEntity(com.salesforce.dva.argus.entity.Subscription subscription) {
        Subscription result = Converter.fromEntity(Subscription.class, subscription);
        return result;
    }

    private final WaaSService waaSService;

    @Context
    UriInfo uriInfo;

    public SubscriptionResource() {
        this.waaSService = system.getServiceFactory().getWaaSService();
    }

    /**
     * Creates a new subscription.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param subscription The subscription object. Cannot be null.
     *
     * @return The resulting list of resources.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @Description("Creates a new subscription.")
    public List<Resource<Subscription>> createSubscription(@Context HttpServletRequest req, Subscription subscription) {
        requireThat(subscription != null, "The subscription cannot be null.");
        PrincipalUser remoteUser = getRemoteUser(req);

        com.salesforce.dva.argus.entity.Subscription entity = new com.salesforce.dva.argus.entity.Subscription(
                remoteUser, subscription.getHostname(), subscription.getPort());
        List<Resource<Subscription>> result = new ArrayList<>();
        Resource<Subscription> res = new Resource<>();
        String message, devMessage;
        Integer status;
        URI uri;
        try {
            entity = waaSService.updateSubscription(entity);
            message = devMessage = "Subscription was successfully created.";
            status = OK.getStatusCode();
            uri = uriInfo.getAbsolutePathBuilder().path(entity.getId().toString()).build();
            res.setEntity(SubscriptionResource.fromEntity(entity));
        } catch (Exception ex) {
            message = "Failed to create subscription.";
            devMessage = ex.getMessage();
            status = BAD_REQUEST.getStatusCode();
            uri = uriInfo.getAbsolutePath();
        }
        res.setMeta(createMetadata(uri, status, req.getMethod(), message, message, devMessage));
        result.add(res);
        return result;
    }

    /**
     * Deletes a client subscription.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param subscriptionId The subscription ID. Cannot be null.
     *
     * @return The resulting list of resources.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{subscriptionId}")
    @Description("Retrieves selected subscription.")
    public List<Resource<Subscription>> getSubscription(@Context HttpServletRequest req,
            @PathParam("subscriptionId") BigInteger subscriptionId) {
        requireThat(subscriptionId != null, "Subscription ID cannot be null.");
        com.salesforce.dva.argus.entity.Subscription entity = waaSService.getSubscription(subscriptionId);
        List<Resource<Subscription>> result = new ArrayList<>();
        Resource<Subscription> res = new Resource<>();
        String message;
        Integer status;
        URI uri = uriInfo.getAbsolutePathBuilder().build();
        if (entity == null) {
            status = NOT_FOUND.getStatusCode();
            message = "Subscription does not exist.";
        } else if (!Objects.equal(entity.getCreatedBy(), getRemoteUser(req))) {
            status = Status.UNAUTHORIZED.getStatusCode();
            message = "You are not authorized to view the resource.";
        } else {
            status = Status.OK.getStatusCode();
            message = "Subscription retrieved successfully.";
            res.setEntity(SubscriptionResource.fromEntity(entity));
        }
        res.setMeta(createMetadata(uri, status, req.getMethod(), message, message, message));
        result.add(res);
        return result;
    }

    /**
     * Deletes a client subscription.
     *
     * @param req The HttpServlet request object. Cannot be null.
     * @param subscriptionId The subscription ID. Cannot be null.
     *
     * @return The resulting list of resources.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{subscriptionId}")
    @Description("Deletes selected subscription associated with this client.")
    public List<Resource<Subscription>> deleteSubscription(@Context HttpServletRequest req,
            @PathParam("subscriptionId") BigInteger subscriptionId) {
        requireThat(subscriptionId != null, "Subscription ID cannot be null.");
        com.salesforce.dva.argus.entity.Subscription entity = waaSService.getSubscription(subscriptionId);
        List<Resource<Subscription>> result = new ArrayList<>();
        Resource<Subscription> res = new Resource<>();
        String message, devMessage;
        Integer status;
        URI uri = uriInfo.getAbsolutePathBuilder().build();
        if (entity == null) {
            status = NOT_FOUND.getStatusCode();
            message = devMessage = "Subscription does not exist.";
        } else if (!Objects.equal(entity.getCreatedBy(), getRemoteUser(req))) {
            status = Status.UNAUTHORIZED.getStatusCode();
            message = devMessage = "You are not authorized to delete the resource.";
        } else {
            try {
                waaSService.deleteSubscription(entity);
                status = Status.OK.getStatusCode();
                message = devMessage = "Subscription deleted successfully.";
                res.setEntity(SubscriptionResource.fromEntity(entity));
            } catch (Exception ex) {
                status = Status.BAD_REQUEST.getStatusCode();
                message = "Failed to delete subscription.";
                devMessage = ex.getMessage();
            }
        }
        res.setMeta(createMetadata(uri, status, req.getMethod(), message, message, devMessage));
        result.add(res);
        return result;
    }

}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
