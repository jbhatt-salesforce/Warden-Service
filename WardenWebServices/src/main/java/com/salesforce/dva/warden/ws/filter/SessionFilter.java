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

package com.salesforce.dva.warden.ws.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.MDC;

/**
 * Establishes a session if required.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class SessionFilter implements Filter {

    private static final String SESSION_ATTRIBUTE_NAME = "SESSION";

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String sessionId;

        if (HttpServletRequest.class.isAssignableFrom(request.getClass())) {
            HttpServletRequest req = HttpServletRequest.class.cast(request);
            HttpSession session = req.getSession(true);

            sessionId = (session == null) ? null : session.getId();
        } else {
            sessionId = null;
        }

        try {
            MDC.put(SESSION_ATTRIBUTE_NAME, sessionId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(SESSION_ATTRIBUTE_NAME);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



