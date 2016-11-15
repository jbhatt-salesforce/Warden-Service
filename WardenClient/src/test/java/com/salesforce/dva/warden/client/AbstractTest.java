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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dva.warden.client.WardenHttpClient.RequestType;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

public abstract class AbstractTest {

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        MAPPER.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
    }

    protected void processRequest(HttpRequestResponse step) { }

    WardenHttpClient getMockedClient(String jsonFile) {
        try {
            String endpoint = "https://localhost:8080/wardenws";
            WardenHttpClient client = spy(new WardenHttpClient(endpoint, 10, 10, 10));
            HttpRequestResponse[] steps = MAPPER.readValue(AbstractTest.class.getResource(jsonFile), HttpRequestResponse[].class);

            for (HttpRequestResponse step : steps) {
                HttpResponse mockedResponse = mock(HttpResponse.class);
                StatusLine mockedStatusLine = mock(StatusLine.class);

                when(mockedStatusLine.getStatusCode()).thenReturn(step.status);
                when(mockedStatusLine.getReasonPhrase()).thenReturn(step.message);
                when(mockedResponse.getEntity()).thenReturn(new StringEntity(step.jsonOutput));
                when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
                doReturn(mockedResponse).when(client).doHttpRequest(step.type, endpoint + step.endpoint, step.jsonInput);
                if (step.getEndpoint().matches("/policy/\\d*/user/.*/metric")) {
                    doAnswer(new Answer() {

                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                processRequest(step);
                                return mockedResponse;
                            }
                        }).when(client).doHttpRequest(eq(RequestType.PUT), eq(endpoint + step.endpoint), any());
                }
            }
            return client;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected static class HttpRequestResponse {

        private WardenHttpClient.RequestType type;
        private String endpoint;
        private String jsonInput;
        private int status;
        private String message;
        private String jsonOutput;

        private HttpRequestResponse() { }

        public String getEndpoint() {
            return endpoint;
        }

        public String getJsonInput() {
            return jsonInput;
        }

        public String getJsonOutput() {
            return jsonOutput;
        }

        public String getMessage() {
            return message;
        }

        public int getStatus() {
            return status;
        }

        public WardenHttpClient.RequestType getType() {
            return type;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setJsonInput(String jsonInput) {
            this.jsonInput = jsonInput;
        }

        public void setJsonOutput(String jsonOutput) {
            this.jsonOutput = jsonOutput;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public void setType(WardenHttpClient.RequestType type) {
            this.type = type;
        }
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
