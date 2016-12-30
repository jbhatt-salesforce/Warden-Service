/* Copyright (c) 2015-2016, Salesforce.com, Inc.
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
package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.dto.User;
import org.junit.Test;
import java.io.IOException;

import static org.junit.Assert.*;

public class AuthServiceTest extends AbstractTest {

    @Test
    public void testBadLogin() throws IOException {
        try(WardenService wardenService = new WardenService(getMockedClient("/AuthServiceTest.testLoginLogout.json"))) {
            AuthService authService = wardenService.getAuthService();
            WardenResponse<User> loginResult = authService.login("aBadUsername", "aBadPassword");

            assertEquals(403, loginResult.getStatus());
        }
    }

    @Test
    public void testLoginLogout() throws IOException {
        try(WardenService wardenService = new WardenService(getMockedClient("/AuthServiceTest.testLoginLogout.json"))) {
            AuthService authService = wardenService.getAuthService();
            WardenResponse<User> loginResult = authService.login("aUsername", "aPassword");
            assertEquals(200, loginResult.getStatus());
            User remoteUser = loginResult.getResources().get(0).getEntity();

            WardenResponse<User> logoutResult = authService.logout();

            assertEquals(200, logoutResult.getStatus());
            assertEquals(remoteUser, logoutResult.getResources().get(0).getEntity());
        }
    }
    
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
