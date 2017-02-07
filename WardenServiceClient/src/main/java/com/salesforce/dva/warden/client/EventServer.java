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

import java.net.InetSocketAddress;
import com.salesforce.dva.warden.client.DefaultWardenClient.InfractionCache;
import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;

/**
 * Receives suspension infraction records from the server and updates the local infraction cache.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class EventServer {

    private final int _port;
    private final InfractionCache _infractions;
    private final EventLoopGroup _bossGroup;    // (1)
    private final EventLoopGroup _workerGroup;

    /**
     * Creates a new EventServer object.
     *
     * @param  port         The port on which to receive infractions. Must be a valid port number.
     * @param  infractions  The infraction cache to update when suspension events are received. Must be thread safe.
     */
    public EventServer(int port, InfractionCache infractions) {
        requireThat((port > 0) && (port <= 65535), "Invalid port number.");
        requireThat(infractions != null, "The infraction cache cannot be null.");

        _port = port;
        _infractions = infractions;
        _bossGroup = new OioEventLoopGroup(100);
        _workerGroup = new OioEventLoopGroup(100);
    }

    /**
     * Shuts down the server.
     *
     * @throws  Exception  If an error occurs.
     */
    public void close() throws Exception {
        _bossGroup.shutdownGracefully().await();
        _workerGroup.shutdownGracefully().await();
    }

    /**
     * Starts the server.
     *
     * @throws  InterruptedException  If interrupted while starting up.
     */
    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();

        b.group(_bossGroup, _workerGroup).channel(OioServerSocketChannel.class).localAddress(new InetSocketAddress(_port)).childHandler(new ChannelInitializer<SocketChannel>() {

                           @Override
                           public void initChannel(SocketChannel ch) throws Exception {
                               ch.pipeline().addLast(new JsonObjectDecoder());
                               ch.pipeline().addLast(new EventServerHandler(_infractions));
                           }

                       });
        b.bind().sync();
    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */
