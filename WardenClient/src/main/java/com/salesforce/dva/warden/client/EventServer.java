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

import com.salesforce.dva.warden.client.DefaultWardenClient.InfractionCache;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import java.net.InetSocketAddress;

/**
 * Created by jbhatt on 11/21/16.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class EventServer {

    //~ Instance fields ******************************************************************************************************************************

    private int port;
    private final InfractionCache infractions;
    EventLoopGroup bossGroup; // (1)
    EventLoopGroup workerGroup;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new EventServer object.
     *
     * @param  port         DOCUMENT ME!
     * @param  infractions  DOCUMENT ME!
     */
    public EventServer(int port, InfractionCache infractions) {
        this.port = port;
        this.infractions = infractions;
        bossGroup = new OioEventLoopGroup(100); // (1)
        workerGroup = new OioEventLoopGroup(100);
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void close() throws Exception {
        bossGroup.shutdownGracefully().await();
        workerGroup.shutdownGracefully().await();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  InterruptedException  DOCUMENT ME!
     */
    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap(); // (2)

        b.group(bossGroup, workerGroup).channel(OioServerSocketChannel.class).localAddress(new InetSocketAddress(port)) // (3)
        .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new JsonObjectDecoder());
                    ch.pipeline().addLast(new EventServerHandler(infractions));
                }
            });
        b.bind().sync(); // (7)
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
