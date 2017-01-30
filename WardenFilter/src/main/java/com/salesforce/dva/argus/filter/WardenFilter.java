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
	 
package com.salesforce.dva.argus.filter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.salesforce.dva.argus.filter.util.PolicyListDeserializer;
import com.salesforce.dva.argus.system.SystemException;
import com.salesforce.dva.warden.SuspendedException;
import com.salesforce.dva.warden.WardenClient;
import com.salesforce.dva.warden.client.WardenClientBuilder;
import com.salesforce.dva.warden.dto.Policy;

import joptsimple.internal.Strings;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class WardenFilter implements Filter {

    //~ Static fields/initializers *******************************************************************************************************************
	private static final String CONFIG_ENDPOINT = "endpoint";
	public static final String USER_ATTRIBUTE_NAME = "USER"; 
	public static final String CONFIG_USERNAME = "userName";
	public static final String CONFIG_PASSWORD = "password";
	public static final String CONFIG_PORT = "port";
	public static final String CONFIG_SERVICE = "service";
	public static final String JSON_LOCATION = "json_location";
	
    private WardenClient wc;
    ListMultimap<String, Policy> policyDef = ArrayListMultimap.create();
    String user = null;
	String url = null;
	String verb = null;
	String json_location = null;
	String service = null;
    private final ObjectMapper _mapper = getMapper();
    protected Logger _logger = LoggerFactory.getLogger(getClass());
    //~ Methods **************************************************************************************************************************************

    @Override
    public void destroy() { }

    /**
     * Authenticates a user if required.
     *
     * @param   request   The HTTP request.
     * @param   response  The HTTP response.
     * @param   chain     The filter chain to execute.
     *
     * @throws  IOException       If an I/O error occurs.
     * @throws  ServletException  If an unknown error occurs.
     */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// examine req, get url,verb
		assertThat(HttpServletRequest.class.isAssignableFrom(request.getClass()), "This is not valid request.",
				Strings.EMPTY);

		HttpServletRequest req = HttpServletRequest.class.cast(request);
		verb = req.getMethod();
		url = req.getRequestURI();
		
		// get remote userName
		HttpSession session = req.getSession(true);
		Object remoteUser = session.getAttribute(USER_ATTRIBUTE_NAME);
		// if remote user is empty, log it
		assertThat(remoteUser != null, Strings.EMPTY,
				"The remote user is null! Please take actions if you want to decline this request.");

		if (remoteUser != null) {
			String user = String.valueOf(remoteUser);

			// match url, verb to a policy
			Map<String, List<Policy>> matchedPolicyDef = policyDef.asMap().entrySet().stream()
					.filter(e -> matchKeyComponents(e.getKey(), url, verb))
					.collect(Collectors.toMap(e -> (String) e.getKey(), e -> (List<Policy>) e.getValue()));

			// call wc.updateMetric(policy, value) ---> client method
			List<List<Policy>> listOfPolicies = matchedPolicyDef.entrySet().stream().map(Map.Entry::getValue)
					.collect(Collectors.toList());

			Set<Policy> flatUniquePolicies = listOfPolicies.stream().flatMap(List::stream).collect(Collectors.toSet());

			// update or catch a suspenedException with returning a response
			Lists.newArrayList(flatUniquePolicies).stream().filter(p -> p.getUsers().contains(user)).forEach(p -> {
				try {
					wc.modifyMetric(p, user, 1);
				} catch (SuspendedException e) {
					HttpServletResponse httpresponse = HttpServletResponse.class.cast(response);
					Object[] params = { e.getUser(), e.getPolicy().getName(), e.getValue(), e.getExpires() };
					String format = "User {0} is suspended for policy {1} after {2} requests, "
							+ "suspension will expires in {3} milliseconds, please take some actions accordingly!\n"
							+ "Warning: expires in -1 milliseconds means this user is suspended permanately, "
							+ "please contact with admin to reinstate this user if needed.";

					String responseMessage =  MessageFormat.format(format, params);
					try {
						httpresponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, responseMessage);
					} catch (Exception e1) {
						_logger.error("IOException occurs when sending back a response.");
					}
				}
			});
		}

		chain.doFilter(request, response);
	}
    
   
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { 
    	//read Filter to get policy DEF's
    	String json_location = getConfigValue(filterConfig, JSON_LOCATION);
    	ListMultimap<String, Policy> policyDef = createPolicyDef(json_location);
    	
    	//read endpoint
    	String endpoint = getConfigValue(filterConfig, CONFIG_ENDPOINT);
    	
    	//instantiate client ---->client methods
    	String userName = getConfigValue(filterConfig, CONFIG_USERNAME);
    	String password = getConfigValue(filterConfig, CONFIG_PASSWORD);
    	try {
			wc = new WardenClientBuilder().forEndpoint(endpoint).withPassword(password).withUsername(userName).build();
		} catch (IOException e) {
			_logger.error("Fail to initiate warden client!", e);
		}
    	
    	//wc.register(policies, port) ------> client methods
    	List<Policy> policies = new ArrayList<>(ImmutableSet.copyOf(policyDef.values()));
    	try {
    		
			wc.register(policies);
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}  	
    }
    
    //~ Helper Methods*******************************************************
    private String getConfigValue(FilterConfig filterConfig, String str) {
    	return filterConfig.getServletContext().getInitParameter(str);
    }
    
    private boolean matchKeyComponents(String key, String url, String verb) {
        assertThat(key != null, "Key cannot be null.", Strings.EMPTY);
        assertThat(url != null, "URL cannot be null.", Strings.EMPTY);
        assertThat(verb != null, "VERB cannot be null.", Strings.EMPTY);

        String[] components = key.split(":");        
        return url.matches(components[0]) && verb.matches(components[1]);
    }
    
    private ListMultimap<String, Policy> createPolicyDef(String json_location){
    	//retrieve value for policies key
    	String allPolicies;
		try {
			allPolicies = getPoliciesJsonStr(json_location);
		} catch (IOException e) {			
			_logger.error("Failure in parsing policies Json!!!",e);
			throw new RuntimeException("Failure in parsing policies Json!!!");
		}
    	
    	ListMultimap<String, Policy> policyDef = toEntity(allPolicies,
                 new TypeReference<ListMultimap<String, Policy>>() { });
    	
    	return policyDef;    	 
    }  
    
    private ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        
        module.addDeserializer(ListMultimap.class, new PolicyListDeserializer());     
        
        mapper.registerModule(module);
        return mapper;
    }
    
	/*
	 * Helper method to convert JSON file to the corresponding policy entity and
	 * key string.
	 */
	private <T> T toEntity(String content, TypeReference<T> type) {
		try {
			return _mapper.readValue(content, type);
		} catch (IOException ex) {
			throw new SystemException(ex);
		}
	}

	private String getPoliciesJsonStr(String json_location)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		URL url = new URL(json_location);
		String policiesJson = mapper.readValue(url, String.class);
		JSONObject policiesJsonObject = new JSONObject(policiesJson);
		String allPolicies = policiesJsonObject.getString("policies");
		return allPolicies;
	}

	// assertion either throwing an exception or logging an error, or do both
	private void assertThat(boolean condition, String exceptionMessage, String logMessage) {
		if (!condition) {
			if (!exceptionMessage.isEmpty() && exceptionMessage != null) {
				throw new IllegalArgumentException(exceptionMessage);
			}

			if (!logMessage.isEmpty() && logMessage != null) {
				_logger.error(logMessage);
			}
		}

	}
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
