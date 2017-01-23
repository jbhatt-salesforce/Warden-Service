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

package com.salesforce.dva.warden.client;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import static org.junit.Assert.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dva.warden.client.DefaultWardenClient.InfractionCache;
import com.salesforce.dva.warden.dto.Infraction;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class EventListenerTest {

    @Test
    public void testMultipleEvents() throws Exception {
        InfractionCache infractions = new InfractionCache();
        int[] ports = { 4444, 5555, 6666, 7777 };
        int threadCount = 20;
        int eventCount = 200;
        Thread[] threads = new Thread[threadCount];

        for (int port : ports) {
            EventServer server = new EventServer(port, infractions);
            EventClient client = new EventClient(port);
            CountDownLatch startingGate = new CountDownLatch(1);

            try {
                server.start();
                client.start();

                for (int i = 0; i < threads.length; i++) {
                    Thread thread = new Thread(new Runnable() {

                                                   @Override
                                                   public void run() {
                                                       try {
                                                           startingGate.await();
                                                       } catch (InterruptedException ex) {
                                                           return;
                                                       }

                                                       for (int j = 0; j < eventCount; j++) {
                                                           try {
                                                               Thread.sleep(50);

                                                               Infraction infraction = new Infraction();

                                                               infraction.setPolicyId(BigInteger.valueOf(System.currentTimeMillis()));
                                                               infraction.setUsername(Thread.currentThread().getId() + "." + j);
                                                               infraction.setExpirationTimestamp(System.currentTimeMillis() + 300000);
                                                               client.sendInfraction(infraction);
                                                           } catch (Exception ex) {
                                                               throw new RuntimeException(ex);
                                                           }
                                                       }
                                                   }

                                               });

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
            } catch (Exception ex) {
                ex.printStackTrace();

                continue;
            } finally {
                client.close();
                server.close();
            }    // end try-catch-finally

            assertFalse(infractions.isEmpty());
            assertEquals(threadCount * eventCount, infractions.size());

            return;
        }    // end for

        fail("No available port found.");
    }

    @Test
    public void testRun() throws Exception {
        InfractionCache infractions = new InfractionCache();
        int[] ports = { 4444, 5555, 6666, 7777 };

        for (int port : ports) {
            EventServer eventServer = new EventServer(port, infractions);
            EventClient eventClient = new EventClient(port);

            try {
                eventServer.start();
                eventClient.start();

                Infraction infraction = new Infraction();

                infraction.setPolicyId(BigInteger.ONE);
                infraction.setUsername("hpotter");
                infraction.setExpirationTimestamp(System.currentTimeMillis() + 300000);
                eventClient.sendInfraction(infraction);
            } catch (Exception ex) {
                continue;
            } finally {
                eventClient.close();
                eventServer.close();
            }

            assertFalse(infractions.isEmpty());
            assertTrue(infractions.size() == 1);

            return;
        }

        fail("No available port found.");
    }

    private static class EventClient {

        private Channel channel;
        private EventLoopGroup workerGroup;
        private final int port;

        public EventClient(int port) {
            this.port = port;
            workerGroup = new NioEventLoopGroup(100);
            channel = null;
        }

        public void close() throws Exception {
            workerGroup.shutdownGracefully().await();
        }

        public void sendInfraction(Infraction infraction) throws Exception {
            if (channel != null) {
                channel.writeAndFlush(new ObjectMapper().writeValueAsString(infraction));
            }
        }

       public void start() throws Exception {
            Bootstrap b = new Bootstrap();

            b.group(workerGroup)
             .channel(NioSocketChannel.class)
             .remoteAddress(InetAddress.getLocalHost(), port)
             .option(ChannelOption.SO_SNDBUF, 1024)
             .handler(new ChannelInitializer<SocketChannel>() {

                          @Override
                          public void initChannel(SocketChannel ch) throws Exception {
                              ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                              ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {

                                             @Override
                                             public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                                 if (!ctx.channel().isWritable()) {
                                                     ctx.flush();
                                                 }

                                                 ctx.write(msg);
                                             }

                                         });
                          }
                          @Override
                          public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                              cause.printStackTrace();
                              ctx.close();
                          }

                      });

            channel = b.connect().sync().channel();
        }

    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



