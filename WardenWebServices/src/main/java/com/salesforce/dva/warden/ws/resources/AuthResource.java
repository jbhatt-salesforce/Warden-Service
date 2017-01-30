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

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import com.salesforce.dva.argus.entity.PrincipalUser;
import com.salesforce.dva.warden.dto.Credentials;
import com.salesforce.dva.warden.dto.Resource;
import com.salesforce.dva.warden.dto.User;
import com.salesforce.dva.warden.ws.filter.AuthFilter;
import com.salesforce.dva.warden.ws.resources.AbstractResource.Description;

/**
 * Provides methods to authenticate users.
 *
 * @author  Bhinav Sura (bsura@salesforce.com)
 */
@Path("/auth")
@Description("Provides methods to authenticate users.")
public class AuthResource extends AbstractResource {

    /**
     * Authenticates a user session.
     *
     * @param   req    The HTTP request.  Cannot be null.
     * @param   creds  The credentials with which to authenticate.  Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     *
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Description("Authenticates a user session.")
    @Path("/login")
    public List<Resource<User>> login(@Context HttpServletRequest req, final Credentials creds) {
        requireThat(creds != null, "You must supply login credentials.");

        try {
            PrincipalUser user = authService.getUser(creds.getUserName(), creds.getPassword());

            requireThat(user != null, "Authorization failed.", Status.UNAUTHORIZED);

            User result = fromEntity(user);

            req.getSession(true).setAttribute(AuthFilter.USER_ATTRIBUTE_NAME, result);
        } catch (Exception ex) {
            requireThat(false, "Authorization failed.", Status.UNAUTHORIZED);
        }

        return rc.getResource(UserResource.class).getUserByUserName(req, creds.getUserName());
    }

    /**
     * Terminates a user session.
     *
     * @param   req  The HTTP request.  Cannot be null.
     *
     * @return The resulting list of resources.  Will never be null, but may be empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Description("Terminates a user session.")
    @Path("/logout")
    public List<Resource<User>> logout(@Context HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        String remoteUser = User.class.cast(session.getAttribute(AuthFilter.USER_ATTRIBUTE_NAME)).getUserName();
        List<Resource<User>> result = new ArrayList<>();

        if (remoteUser != null) {
            result.addAll(rc.getResource(UserResource.class).getUserByUserName(req, remoteUser));
        }

        session.removeAttribute(AuthFilter.USER_ATTRIBUTE_NAME);
        session.invalidate();

        return result;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



