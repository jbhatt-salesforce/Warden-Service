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
import org.junit.Test;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/** @todo multithreaded stress test. */
public class EventListenerTest {

    @Test
    public void testRun() throws IOException, InterruptedException {
        LinkedHashMap<String, Infraction> infractions = new LinkedHashMap<>();
        int[] ports = { 4444, 5555, 6666, 7777 };

        for (int port : ports) {
            DatagramSocket socket = null;

            try {
                EventListener listener = new EventListener(infractions, port);

                listener.setDaemon(true);
                listener.start();
                socket = new DatagramSocket();

                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, 1024);

                packet.setAddress(InetAddress.getLocalHost());
                packet.setPort(port);

                Infraction infraction = new Infraction();

                infraction.setPolicyId(BigInteger.ONE);
                infraction.setUserName("hpotter");
                packet.setData(new ObjectMapper().writeValueAsBytes(infraction));
                socket.send(packet);
                listener.interrupt();
                listener.join(10000);
                assertFalse(infractions.isEmpty());
                return;
            } catch (SocketException ex) {
                assert true : "Try the next port";
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
        fail("No available port found.");
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
