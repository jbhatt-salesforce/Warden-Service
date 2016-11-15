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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dva.warden.dto.Infraction;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

/**
 * Created by jbhatt on 10/12/16.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
class EventListener extends Thread {

    //~ Instance fields ******************************************************************************************************************************

    Map<String, Infraction> _infractions;
    WardenService _wardenService;
    int _port;
    DatagramSocket _socket;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new EventListener object.
     *
     * @param   infractions    DOCUMENT ME!
     * @param   wardenService  DOCUMENT ME!
     * @param   port           DOCUMENT ME!
     *
     * @throws  SocketException  DOCUMENT ME!
     */
    EventListener(Map<String, Infraction> infractions, WardenService wardenService, int port) throws SocketException {
        this._infractions = infractions;
        this._wardenService = wardenService;
        this._port = port;
        this._socket = new DatagramSocket(_port);
    }

    //~ Methods **************************************************************************************************************************************

    @Override
    public void interrupt() {
        super.interrupt(); // To change body of generated methods, choose Tools | Templates.
        _socket.close();
    }

    @Override
    public void run() {
        long delta = 0;

        while (!Thread.interrupted()) {
            Thread.yield();
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                _socket.receive(packet);

                ObjectMapper mapper = new ObjectMapper();
                Infraction infraction = mapper.readValue(packet.getData(), Infraction.class);

                _infractions.put(DefaultWardenClient.createKey(infraction.getPolicyId(), infraction.getUserName()), infraction);
            } catch (IOException e) {
                interrupt();
                throw new RuntimeException(e);
            }
        }
        _infractions = null;
        _wardenService = null;
        _socket = null;
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
