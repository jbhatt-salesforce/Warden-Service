package com.salesforce.dva.argus.filter.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.salesforce.dva.warden.dto.Policy;
/**
 * Policy configuration to store information from policy def json
 * extend policy dto by adding url and verb
 * 
 * @author rzhang
 *
 */
@JsonTypeName("policyConfig")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyConfig extends Policy{

	private String url;
	private String verb;
	
	
	//~ Methods **************************************************************************************************************************************

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

    
    @Override
    public PolicyConfig createExample() {
        PolicyConfig result = new PolicyConfig();

        result.setId(BigInteger.ONE);
        result.setCreatedById(BigInteger.ONE);
        result.setCreatedDate(new Date(1451606400000L));
        result.setModifiedById(BigInteger.TEN);
        result.setModifiedDate(new Date(1451606400000L));
        result.setService("example-service");
        result.setName("example-name");
        result.setOwners(Arrays.asList("example-owners"));
        result.setUsers(Arrays.asList("example-users"));
        result.setSubSystem("example-subSystem");
        result.setTriggerType(TriggerType.NOT_BETWEEN);
        result.setAggregator(Aggregator.SUM);
        result.setThresholds(Arrays.asList(0.0));
        result.setTimeUnit("5min");
        result.setDefaultValue(0.0);
        result.setCronEntry("0 */4 * * *");
        result.setVerb("verb");
        result.setUrl("url");
        return result;
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

        final PolicyConfig other = (PolicyConfig) obj;

        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.verb, other.verb)) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 7;

        hash = 13 * hash + super.hashCode();
        hash = 13 * hash + Objects.hashCode(this.url);
        hash = 13 * hash + Objects.hashCode(this.verb);
        return hash;
    }
}