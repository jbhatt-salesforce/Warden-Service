package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.dto.Infraction;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jbhatt on 11/21/16.
 */
public class EventServer {

    private int _port;
    private Map<String, Infraction> _infractions;

    private ChannelFuture channelFuture;
    private ServerBootstrap b = new ServerBootstrap();


    public EventServer (int port, Map<String, Infraction> infractions) throws SocketException {
        this._port = port;
        this._infractions = infractions;
    }

    public void start() throws Exception{
        //start listening
        // Bind and start to accept incoming connections.
        try {
            channelFuture = b.bind(_port).sync();
        } catch (Exception e){

        }
    }

    public void stop() throws Exception{
        //stop listening

        // Wait until the server socket is closed.
        // In this example, this does not happen, but you can do that to gracefully
        // shut down your server.
        try {
        channelFuture.channel().closeFuture().sync();
        } catch (Exception e){}
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ServerHandler(_infractions));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
