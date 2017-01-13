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
package com.salesforce.dva.warden.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Usage policy record.
 *
 * @author Jigna Bhatt (jbhatt@salesforce.com)
 */
@JsonTypeName("policy")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Policy extends Entity {

    //~ Static fields/initializers *******************************************************************************************************************
    private static final long serialVersionUID = 1L;

    //~ Instance fields ******************************************************************************************************************************
    private String service;
    private String name;
    private List<String> owners = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private String subSystem;
    private TriggerType triggerType;
    private Aggregator aggregator;
    private List<Double> threshold;
    private String timeUnit;
    private Double defaultValue;
    private String cronEntry;
    private List<SuspensionLevel> suspensionLevels = new ArrayList<>();

    //~ Methods **************************************************************************************************************************************
    @Override
    public Policy createExample() {
        Policy result = new Policy();

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

        final Policy other = (Policy) obj;

        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.service, other.service)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.subSystem, other.subSystem)) {
            return false;
        }
        if (!Objects.equals(this.timeUnit, other.timeUnit)) {
            return false;
        }
        if (!Objects.equals(this.cronEntry, other.cronEntry)) {
            return false;
        }
        if (!Objects.equals(this.owners, other.owners)) {
            return false;
        }
        if (!Objects.equals(this.users, other.users)) {
            return false;
        }
        if (this.triggerType != other.triggerType) {
            return false;
        }
        if (this.aggregator != other.aggregator) {
            return false;
        }
        if (!Objects.equals(this.threshold, other.threshold)) {
            return false;
        }
        if (!Objects.equals(this.defaultValue, other.defaultValue)) {
            return false;
        }
        if (!Objects.equals(this.suspensionLevels, other.suspensionLevels)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the aggregator to use when combining the policy metric across different sources.
     *
     * @return The aggregator.
     */
    public Aggregator getAggregator() {
        return aggregator;
    }

    /**
     * Returns the CRON entry describing the frequency of evaluation.
     *
     * @return The CRON entry.
     */
    public String getCronEntry() {
        return cronEntry;
    }

    /**
     * Returns the default value for the policy metric.
     *
     * @return The default value.
     */
    public Double getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the policy name.
     *
     * @return The policy name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of owners for the policy.
     *
     * @return The list of owners.
     */
    public List<String> getOwners() {
        return owners;
    }

    /**
     * Returns the name of the service with which the policy is associated.
     *
     * @return The service name.
     */
    public String getService() {
        return service;
    }

    /**
     * Returns the service subsystem with which the policy is associated.
     *
     * @return The subsystem name.
     */
    public String getSubSystem() {
        return subSystem;
    }

    /**
     * Returns the suspension levels associated with the policy.
     *
     * @return The suspension levels.
     */
    public List<SuspensionLevel> getSuspensionLevels() {
        return suspensionLevels;
    }

    /**
     * Returns the thresholds that determine an infraction has occurred.
     *
     * @return The policy thresholds.
     */
    public List<Double> getThresholds() {
        return threshold;
    }

    /**
     * Returns the time window used to evaluate the policy for an infraction.
     *
     * @return The time window.
     */
    public String getTimeUnit() {
        return timeUnit;
    }

    /**
     * Returns the trigger type used to evaluate the policy for an infraction.
     *
     * @return The trigger type.
     */
    public TriggerType getTriggerType() {
        return triggerType;
    }

    /**
     * Returns the list of users to which the policy applies.
     *
     * @return The list of users.
     */
    public List<String> getUsers() {
        return users;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 13 * hash + super.hashCode();
        hash = 13 * hash + Objects.hashCode(this.service);
        hash = 13 * hash + Objects.hashCode(this.name);
        hash = 13 * hash + Objects.hashCode(this.owners);
        hash = 13 * hash + Objects.hashCode(this.users);
        hash = 13 * hash + Objects.hashCode(this.subSystem);
        hash = 13 * hash + Objects.hashCode(this.triggerType);
        hash = 13 * hash + Objects.hashCode(this.aggregator);
        hash = 13 * hash + Objects.hashCode(this.threshold);
        hash = 13 * hash + Objects.hashCode(this.timeUnit);
        hash = 13 * hash + Objects.hashCode(this.defaultValue);
        hash = 13 * hash + Objects.hashCode(this.cronEntry);
        hash = 13 * hash + Objects.hashCode(this.suspensionLevels);
        return hash;
    }

    /**
     * Sets the aggregator to use when combining the policy metric from different sources.
     *
     * @param aggregator The aggregator.
     */
    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
    }

    /**
     * Sets the CRON entry used to evaluate the policy.
     *
     * @param cronEntry The CRON entry.
     */
    public void setCronEntry(String cronEntry) {
        this.cronEntry = cronEntry;
    }

    /**
     * Sets the default value for the policy metric.
     *
     * @param defaultValue The default value.
     */
    public void setDefaultValue(Double defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the name of the policy.
     *
     * @param name The name of the policy.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the list of owners to which the policy applies.
     *
     * @param owner The list of owners to which the policy applies.
     */
    public void setOwners(List<String> owner) {
        this.owners = owner;
    }

    /**
     * Sets the service name.
     *
     * @param service The service name.
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * Sets the service subsystem name.
     *
     * @param subSystem The service subsystem name.
     */
    public void setSubSystem(String subSystem) {
        this.subSystem = subSystem;
    }

    /**
     * Sets the suspension levels for the policy.
     *
     * @param suspensionLevels The suspension levels.
     */
    public void setSuspensionLevels(List<SuspensionLevel> suspensionLevels) {
        this.suspensionLevels = suspensionLevels;
    }

    /**
     * Sets the policy thresholds.
     *
     * @param threshold The policy thresholds.
     */
    public void setThresholds(List<Double> threshold) {
        this.threshold = threshold;
    }

    /**
     * Sets the time window over which the policy will be evaluated.
     *
     * @param timeUnit The time unit.
     */
    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * Sets the trigger type used to evaluate the policy.
     *
     * @param triggerType The trigger type.
     */
    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    /**
     * Sets the list of user to which the policy applies.
     *
     * @param user The list of users.
     */
    public void setUsers(List<String> user) {
        this.users = user;
    }

    //~ Enums ****************************************************************************************************************************************
    /**
     * The supported methods for aggregation and down sampling.
     *
     * @author Tom Valine (tvaline@salesforce.com)
     */
    public enum Aggregator {

        MIN("min"),
        MAX("max"),
        SUM("sum"),
        AVG("avg"),
        DEV("dev"),
        ZIMSUM("zimsum"),
        MINMIN("minmin"),
        MINMAX("minmax");

        private final String _description;

        private Aggregator(String description) {
            _description = description;
        }

        /**
         * Returns the element corresponding to the given name.
         *
         * @param name The aggregator name.
         *
         * @return The corresponding aggregator element.
         */
        public static Aggregator fromString(String name) {
            if ((name != null) && !name.isEmpty()) {
                for (Aggregator aggregator : Aggregator.values()) {
                    if (name.equalsIgnoreCase(aggregator.name())) {
                        return aggregator;
                    }
                }
            }
            return null;
        }

        /**
         * Returns the short hand description of the method.
         *
         * @return The method description.
         */
        public String getDescription() {
            return _description;
        }
    }

    /**
     * The type of trigger.
     *
     * @author Tom Valine (tvaline@salesforce.com)
     */
    public enum TriggerType {

        /**
         * Greater than.
         */
        GREATER_THAN,
        /**
         * Greater than or equal to.
         */
        GREATER_THAN_OR_EQ,
        /**
         * Less than.
         */
        LESS_THAN,
        /**
         * Less than or equal to.
         */
        LESS_THAN_OR_EQ,
        /**
         * Equal to.
         */
        EQUAL,
        /**
         * Not equal to.
         */
        NOT_EQUAL,
        /**
         * Between.
         */
        BETWEEN,
        /**
         * Not between.
         */
        NOT_BETWEEN;

        /**
         * Converts a string to a trigger type.
         *
         * @param name The trigger type name.
         *
         * @return The corresponding trigger type.
         *
         * @throws IllegalArgumentException If no corresponding trigger type is found.
         */
        @JsonCreator
        public static TriggerType fromString(String name) {
            for (TriggerType t : TriggerType.values()) {
                if (t.toString().equalsIgnoreCase(name)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Trigger Type does not exist.");
        }

        /**
         * Returns the name of the trigger type.
         *
         * @return The name of the trigger type.
         */
        @JsonValue
        public String value() {
            return this.toString();
        }
    }

    @Override
    public String toString() {
        return "Policy{" + "id=" + id + ", createdById=" + createdById + ", createdDate=" + createdDate + ", modifiedById=" + modifiedById
                + ", modifiedDate=" + modifiedDate + "service=" + service + ", name=" + name + ", owners=" + owners + ", users=" + users
                + ", subSystem=" + subSystem + ", triggerType=" + triggerType + ", aggregator=" + aggregator + ", threshold=" + threshold
                + ", timeUnit=" + timeUnit + ", defaultValue=" + defaultValue + ", cronEntry=" + cronEntry + ", suspensionLevels="
                + suspensionLevels + '}';
    }

}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
