package com.salesforce.dva.argus.service.alert.notifier;


import java.util.ArrayList;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.salesforce.dva.argus.entity.*;



public final class WaaSNotifierClient {

    public static void doStart(List<Subscription> subscriptions, ByteBuf data) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
        	ArrayList<ChannelFuture> channelFutures = null;
        	for (Subscription s : subscriptions) {
        		Bootstrap b = new Bootstrap();
        		b.group(group)
        		.channel(NioSocketChannel.class)
        		.option(ChannelOption.TCP_NODELAY, true)
        		.handler(new ChannelInitializer<SocketChannel>() {
        			@Override
        			public void initChannel(SocketChannel ch) throws Exception {
        				ChannelPipeline p = ch.pipeline();
        				p.addLast(s.toString(), new WaaSNotifierClientHandler(data));
        			}
        	});
        		
        		channelFutures = new ArrayList<>();
        		ChannelFuture future = b.connect(s.getHostname(), s.getPort()).sync();
        		channelFutures.add(future);
        	}
             
        	for(ChannelFuture channelFuture : channelFutures) {
            	 channelFuture.channel().closeFuture().sync();
             }
        	} finally {
        		group.shutdownGracefully();
        	}
    }
   
}


