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
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import com.salesforce.dva.argus.service.MonitorService;
import com.salesforce.dva.argus.system.SystemMain;
import com.salesforce.dva.warden.ws.listeners.WebServletListener;

/**
 * Servlet filter to push end point performance numbers to monitoring service.
 *
 * @author  Kiran Gowdru (kgowdru@salesforce.com)
 */
public class PerfFilter implements Filter {

    protected final SystemMain system = WebServletListener.getSystem();
    private final MonitorService monitorService = system.getServiceFactory().getMonitorService();
    static private final String DATA_READ_PER_MIN = "perf.ws.read.count";
    static private final String DATA_READ_QUERY_LATENCY = "perf.ws.read.latency";
    static private final String DATA_WRITE_PER_MIN = "perf.ws.write.count";
    static private final String DATA_WRITE_LATENCY = "perf.ws.write.latency";
    static private final String DATA_READ_REQ_BYTES = "perf.ws.read.rxbytes";
    static private final String DATA_READ_RESP_BYTES = "perf.ws.read.txbytes";
    static private final String DATA_WRITE_REQ_BYTES = "perf.ws.write.rxbytes";
    static private final String DATA_WRITE_RESP_BYTES = "perf.ws.write.txbytes";
    static private final String TAGS_METHOD_KEY = "method";
    static private final String TAGS_ENDPOINT_KEY = "endpoint";

    @Override
    public void destroy() {}

    /**
     * Updates performance counters using the Warden monitoring service.
     *
     * @param   request   The HTTP request.
     * @param   response  The HTTP response.
     * @param   chain     The filter chain to execute.
     *
     * @throws  IOException       If an I/O error occurs.
     * @throws  ServletException  If an unknown error occurs.
     *
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = HttpServletRequest.class.cast(request);
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long delta = System.currentTimeMillis() - start;
            HttpServletResponse resp = HttpServletResponse.class.cast(response);

            updateCounters(req, resp, delta);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    private void updateCounters(HttpServletRequest req, HttpServletResponse resp, long delta) {
        String method = req.getMethod();

        try {
            String pathInfo = req.getPathInfo().replaceFirst("/", "");
            String endPoint = pathInfo.replaceAll("[0-9]+", "-");
            Map<String, String> tags = new HashMap<>();

            tags.put(TAGS_METHOD_KEY, method);
            tags.put(TAGS_ENDPOINT_KEY, endPoint);

            String contentLength = resp.getHeader("Content-Length");
            int respBytes = (((contentLength != null) && contentLength.matches("[0-9]+")) ? Integer.parseInt(contentLength) : 0);
            int reqBytes = ((req.getContentLength() > 0) ? req.getContentLength() : 0);

            if (method.equals("GET")) {
                monitorService.modifyCustomCounter(DATA_READ_PER_MIN, 1, tags);
                monitorService.modifyCustomCounter(DATA_READ_QUERY_LATENCY, delta, tags);
                monitorService.modifyCustomCounter(DATA_READ_REQ_BYTES, reqBytes, tags);
                monitorService.modifyCustomCounter(DATA_READ_RESP_BYTES, respBytes, tags);
            } else if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
                monitorService.modifyCustomCounter(DATA_WRITE_PER_MIN, 1, tags);
                monitorService.modifyCustomCounter(DATA_WRITE_LATENCY, delta, tags);
                monitorService.modifyCustomCounter(DATA_WRITE_REQ_BYTES, reqBytes, tags);
                monitorService.modifyCustomCounter(DATA_WRITE_RESP_BYTES, respBytes, tags);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
        }
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */


