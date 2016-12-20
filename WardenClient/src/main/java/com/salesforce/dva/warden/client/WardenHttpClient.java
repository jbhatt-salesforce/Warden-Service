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

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;

import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;

/**
 * Warden specific HTTP client.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 */
class WardenHttpClient {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(WardenHttpClient.class.getName());

    static {
        MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.ANY);
        MAPPER.setVisibility(PropertyAccessor.SETTER, Visibility.ANY);
    }

    //~ Instance fields ******************************************************************************************************************************

    String _endpoint;
    private final CloseableHttpClient _httpClient;
    private final PoolingHttpClientConnectionManager _connMgr;
    private final BasicCookieStore _cookieStore;
    private final BasicHttpContext _httpContext;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new Argus HTTP client.
     *
     * @param   endpoint    The URL of the read _endpoint including the port number. Must not be null.
     * @param   maxConn     The maximum number of concurrent connections. Must be greater than 0.
     * @param   timeout     The connection timeout in milliseconds. Must be greater than 0.
     * @param   reqTimeout  The connection request timeout in milliseconds. Must be greater than 0.
     *
     * @throws  IOException  If a connection cannot be established.
     */
    WardenHttpClient(String endpoint, int maxConn, int timeout, int reqTimeout) throws IOException {
        requireThat(endpoint != null && !endpoint.isEmpty(), "Invalid endpoint.");
        requireThat(maxConn > 0, "Maximum connections must be a positive non-zero number.");
        requireThat(timeout > 0, "Connection timeout must be a positive non-zero number of milliseconds.");
        requireThat(reqTimeout > 0, "Request timeout must be a positive non-zero number of milliseconds.");

        URL url = new URL(endpoint);
        int port = url.getPort();

        _connMgr = new PoolingHttpClientConnectionManager();
        _connMgr.setMaxTotal(maxConn);
        _connMgr.setDefaultMaxPerRoute(maxConn);

        String routePath = endpoint.substring(0, endpoint.lastIndexOf(':'));
        HttpHost host = new HttpHost(routePath, port);
        RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectionRequestTimeout(reqTimeout).setConnectTimeout(timeout).build();

        _connMgr.setMaxPerRoute(new HttpRoute(host), maxConn / 2);
        _httpClient = HttpClients.custom().setConnectionManager(_connMgr).setDefaultRequestConfig(defaultRequestConfig).build();
        _cookieStore = new BasicCookieStore();
        _httpContext = new BasicHttpContext();
        _httpContext.setAttribute(HttpClientContext.COOKIE_STORE, _cookieStore);
        LOGGER.debug("Argus HTTP Client initialized using " + endpoint);
        _endpoint = endpoint;
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Helper method to convert an object to a JSON string.
     *
     * @param   <T>     The object type parameter.
     * @param   object  The object to convert. Cannot be null.
     *
     * @return  The JSON representation of the object.
     *
     * @throws  IOException  If a JSON processing error occurs.
     */
    protected <T> String toJson(T object) throws IOException {
        requireThat(object != null, "The object to convert cannot be null.");
        return MAPPER.writeValueAsString(object);
    }

    /**
     * Closes the client connections and prepares the client for garbage collection. This method may be invoked on a client which has already been
     * disposed.
     *
     * @throws  IOException  If an I/O exception occurs while disposing of the client.
     */
    void dispose() throws IOException {
        _httpClient.close();
        _cookieStore.clear();
        _httpContext.clear();
    }

    /**
     * Submits a request to the service.
     *
     * @param   requestType  The HTTP request type. Cannot be null.
     * @param   url          The web service URL to receive the request. Cannot be null.
     * @param   json         The JSON payload to send. May be null.
     *
     * @return  The corresponding response. Will never be null.
     *
     * @throws  IOException               If an I/O exception occurs.
     * @throws  IllegalArgumentException  If an unsupported request type is specified.
     */
    HttpResponse doHttpRequest(RequestType requestType, String url, String json) throws IOException {
        requireThat(requestType != null, "The request type cannot be null.");
        requireThat(url != null && !url.isEmpty(), "The URL cannot be null or empty.");

        StringEntity entity = null;

        if (json != null) {
            entity = new StringEntity(json);
            entity.setContentType("application/json");
        }
        switch (requestType) {
            case POST:

                HttpPost post = new HttpPost(url);

                post.setEntity(entity);
                return _httpClient.execute(post, _httpContext);
            case GET:

                HttpGet httpGet = new HttpGet(url);

                return _httpClient.execute(httpGet, _httpContext);
            case DELETE:

                HttpDelete httpDelete = new HttpDelete(url);

                return _httpClient.execute(httpDelete, _httpContext);
            case PUT:

                HttpPut httpput = new HttpPut(url);

                httpput.setEntity(entity);
                return _httpClient.execute(httpput, _httpContext);
            default:
                throw new IllegalArgumentException(" Request Type " + requestType + " not a valid request type. ");
        }
    }

    /**
     * Executes the requests and wraps the response object.
     *
     * @param   <T>          The response entity type parameter.
     * @param   requestType  The request type. Cannot be null.
     * @param   url          The URL to receive the request. Cannot be null or empty.
     * @param   payload      The optional payload to send as JSON.
     *
     * @return  The wrapped response object.
     *
     * @throws  IOException  If an I/O exception occurs.
     */
    <T> WardenResponse<T> executeHttpRequest(RequestType requestType, String url, Object payload) throws IOException {
        url = _endpoint + url;

        String json = payload == null ? null : toJson(payload);
        HttpResponse response = doHttpRequest(requestType, url, json);
        WardenResponse<T> wardenResponse = WardenResponse.generateResponse(response);

        return wardenResponse;
    }

    //~ Enums ****************************************************************************************************************************************

    /**
     * The request type to use.
     *
     * @author  Jigna Bhatt (jbhatt@salesforce.com)
     */
    static enum RequestType {

        POST,
        GET,
        DELETE,
        PUT;
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
