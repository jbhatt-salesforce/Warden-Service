package com.salesforce.dva.argus.service.alert.notifier;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class WaaSNotifierClientHandler extends ChannelInboundHandlerAdapter {

    
    private  ByteBuf buf;

    /**
     * Creates a client-side handler.
     */
    public WaaSNotifierClientHandler(ByteBuf data) {
    	buf = data;
    }


	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
    	buf = ctx.alloc().buffer(8192); // (1)
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		buf.release(); // (1)
		buf = null;
	}
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Send the warden event message if this handler is a client-side handler.
        ctx.writeAndFlush(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
