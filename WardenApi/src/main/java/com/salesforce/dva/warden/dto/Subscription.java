/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salesforce.dva.warden.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author tvaline
 */
@SuppressWarnings("serial")
@JsonTypeName("subscription")
public class Subscription extends Entity {
    
    String hostname;
    Integer port;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.hostname);
        hash = 97 * hash + Objects.hashCode(this.port);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Subscription other = (Subscription) obj;
        if (!Objects.equals(this.hostname, other.hostname)) {
            return false;
        }
        if (!Objects.equals(this.port, other.port)) {
            return false;
        }
        return true;
    }
    
    

    @Override
    public Object createExample() {
        Subscription result = new Subscription();
        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date());
        result.setHostname("example.client.com");
        result.setModifiedById(BigInteger.ONE);
        result.setModifiedDate(new Date());
        result.setPort(8080);
        return result;
    }
    
}
