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
import com.salesforce.dva.argus.service.ManagementService;
import com.salesforce.dva.warden.ws.resources.AbstractResource.Description;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Web services for management service.
 *
 * @author  Raj Sarkapally (rsarkapally@salesforce.com)
 */
@Path("/management")
@Description("Provides methods to manage the services.")
public class ManagementResource extends AbstractResource {

    //~ Instance fields ******************************************************************************************************************************

    private final ManagementService managementService = system.getServiceFactory().getManagementService();

    //~ Methods **************************************************************************************************************************************

    /**
     * Updates the administrator privileges for the user.
     *
     * @param   req         The HTTP request.
     * @param   userName    Name of the user whom the administrator privileges will be updated. Cannot be null or empty.
     * @param   privileged  boolean variable indicating administrator privileges.
     *
     * @return  Response object indicating whether the operation was successful or not.
     *
     * @throws  IllegalArgumentException  Throws IllegalArgument exception when the input is not valid.
     * @throws  WebApplicationException   Throws this exception if the user does not exist or the user is not authorized to carry out this operation.
     */
    @PUT
    @Path("/administratorprivilege")
    @Produces(MediaType.APPLICATION_JSON)
    @Description("Grants administrative privileges.")
    public Response setAdministratorPrivilege(@Context HttpServletRequest req,
        @FormParam("username") String userName,
        @FormParam("privileged") boolean privileged) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty.");
        }
        validatePrivilegedUser(req);

        PrincipalUser user = userService.findUserByUsername(userName);

        if (user == null) {
            throw new WebApplicationException("User does not exist.", Status.NOT_FOUND);
        }
        managementService.setAdministratorPrivilege(user, privileged);
        return Response.status(Status.OK).build();
    }
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
