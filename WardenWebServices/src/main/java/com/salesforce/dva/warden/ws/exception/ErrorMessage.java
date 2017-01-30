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

package com.salesforce.dva.warden.ws.exception;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides error code and error message.
 *
 * @author  Raj Sarkapally (rsarkapally@salesforce.com)
 */
@XmlRootElement
class ErrorMessage {

    private int _status;
    private String _message;

    /** Creates a new ErrorMessage object. */
    public ErrorMessage() {}

    /**
     * Creates a new ErrorMessage object.
     *
     * @param  status   The status code.
     * @param  message  The exception message.
     */
    public ErrorMessage(int status, String message) {
        _status = status;
        _message = message;
    }

    /**
     * Returns the error message.
     *
     * @return  Error message
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Sets the error message.
     *
     * @param  message  The error message
     */
    public void setMessage(String message) {
        _message = message;
    }

    /**
     * Returns the status code.
     *
     * @return  The status code
     */
    public int getStatus() {
        return _status;
    }

    /**
     * Sets the status code.
     *
     * @param  status  The status code.
     */
    public void setStatus(int status) {
        _status = status;
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



