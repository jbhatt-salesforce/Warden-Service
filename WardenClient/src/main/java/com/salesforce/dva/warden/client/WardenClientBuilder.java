package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.WardenClient;
import java.io.IOException;


public class WardenClientBuilder {

    private String endpoint;
    private String username;
    private String password;
    private Integer port;

    public WardenClientBuilder() {
    }

    public WardenClientBuilder forEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public WardenClientBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public WardenClientBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public WardenClientBuilder listeningOn(Integer port) {
        this.port = port;
        return this;
    }

    public WardenClient build() throws IOException {
        return new DefaultWardenClient(endpoint, username, password);
    }
    
}
