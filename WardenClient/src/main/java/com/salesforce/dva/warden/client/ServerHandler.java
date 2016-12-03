package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.dto.Infraction;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.Map;

/**
 * Created by jbhatt on 11/29/16.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter  {

        //A filter will call handleEvent method and send a response to the warden server
        //update infraction cache
        //response json
        //status: 1 or 0

        private Map<String, Infraction> _infractions;

        public ServerHandler(Map<String, Infraction> infractions){
            _infractions = infractions;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            ByteBuf in = (ByteBuf) msg;
            try {
                while (in.isReadable()) {
                    System.out.print((char) ((ByteBuf) msg).readByte());
                }
                System.out.flush();
            } finally {
                ReferenceCountUtil.release(msg);
            }
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
}
