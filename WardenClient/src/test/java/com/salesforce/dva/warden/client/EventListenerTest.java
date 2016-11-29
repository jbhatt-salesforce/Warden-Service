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
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class EventListenerTest {

    private static class Client {
        private Client (int port){}
        void sendInfraction(Infraction infraction){

        }
    }
    @Test
    public void testMultipleEvents() throws Exception {
        LinkedHashMap<String, Infraction> infractions = new LinkedHashMap<>();
        int[] ports = { 4444, 5555, 6666, 7777 };
        int threadCount = 20;
        int eventCount = 100;
        Thread[] threads = new Thread[threadCount];

        for (int port : ports) {

            EventServer listener = new EventServer(port, infractions);
            Client client = new Client(port);
            CountDownLatch startingGate = new CountDownLatch(1);

            try {
                listener.start();

                for (int i = 0; i < threads.length; i++) {
                    Thread thread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    startingGate.await();
                                    for (int j = 0; j < eventCount; j++) {
                                        Infraction infraction = new Infraction();

                                        infraction.setPolicyId(BigInteger.ONE);
                                        infraction.setUserName(Thread.currentThread().getId() + "." + j);
                                        //send to server
                                        client.sendInfraction(infraction);
                                    }
                                } catch (InterruptedException ex) {
                                    return;
                                }
                            }
                        });

                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                throw new RuntimeException(e);
                            }
                        });
                    threads[i] = thread;
                    thread.start();
                }
                startingGate.countDown();
                for (int i = 0; i < threads.length; i++) {
                    threads[i].join(10000);
                }
                assertFalse(infractions.isEmpty());
                assertEquals(threadCount * eventCount, infractions.size());
                return;
            } catch (SocketException ex){
                continue;
            }
              finally
             {
                listener.stop();
            } // end try-catch-finally
        } // end for
        fail("No available port found.");
    }

    @Test
    public void testRun() throws Exception {
        LinkedHashMap<String, Infraction> infractions = new LinkedHashMap<>();
        int[] ports = { 4444, 5555, 6666, 7777 };

        for (int port : ports) {
            EventServer eventServer = new EventServer(port, infractions);
            Client client = new Client(port);

            try {
                eventServer.start();

                Infraction infraction = new Infraction();

                infraction.setPolicyId(BigInteger.ONE);
                infraction.setUserName("hpotter");
                //sent packet to server
                client.sendInfraction(infraction);

                assertFalse(infractions.isEmpty());
                assertTrue(infractions.size() == 1);
                return;
            } catch (SocketException ex){
                continue;
            }
            finally {
                eventServer.stop();
            }
        }
        fail("No available port found.");
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
