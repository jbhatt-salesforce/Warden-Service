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
package com.salesforce.dva.warden;

import com.salesforce.dva.warden.dto.Policy;
import java.util.Objects;

/**
 * Indicates an operation was attempted by a suspended user.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class SuspendedException extends Exception {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final long serialVersionUID = 1L;

    //~ Instance fields ******************************************************************************************************************************

    private final Policy _policy;
    private final String _user;
    private final Long _expires;
    private final Double _value;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new SuspendedException object.
     *
     * @param  policy   The policy for which the user was suspended.
     * @param  user     The suspended user.
     * @param  expires  The time at which the suspension expires.
     * @param  value    The value of the policy violation at the time of suspension.
     */
    public SuspendedException(Policy policy, String user, Long expires, Double value) {
        _policy = policy;
        _user = user;
        _expires = expires;
        _value = value;
    }

    //~ Methods **************************************************************************************************************************************

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

        final SuspendedException other = (SuspendedException) obj;

        if (!Objects.equals(this._user, other._user)) {
            return false;
        }
        if (!Objects.equals(this._policy, other._policy)) {
            return false;
        }
        if (!Objects.equals(this._expires, other._expires)) {
            return false;
        }
        if (!Objects.equals(this._value, other._value)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the time at which the suspension expires.
     *
     * @return  The time at which the suspension expires.
     */
    public Long getExpires() {
        return _expires;
    }

    /**
     * Returns the policy for which the user was suspended.
     *
     * @return  The policy.
     */
    public Policy getPolicy() {
        return _policy;
    }

    /**
     * Returns the suspended user.
     *
     * @return  The suspended user.
     */
    public String getUser() {
        return _user;
    }

    /**
     * Returns the value of the policy violation at the time of suspension.
     *
     * @return  The value of the policy violation.
     */
    public Double getValue() {
        return _value;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 17 * hash + Objects.hashCode(this._policy);
        hash = 17 * hash + Objects.hashCode(this._user);
        hash = 17 * hash + Objects.hashCode(this._expires);
        hash = 17 * hash + Objects.hashCode(this._value);
        return hash;
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
