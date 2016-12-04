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
import com.salesforce.dva.warden.client.DefaultWardenClient.InfractionCache;
import com.salesforce.dva.warden.dto.Infraction;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jbhatt on 11/29/16.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class EventServerHandler extends ChannelInboundHandlerAdapter {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final AtomicLong count = new AtomicLong();

    //~ Instance fields ******************************************************************************************************************************

    // A filter will call handleEvent method and send a response to the warden server
    // update infraction cache
    // response json
    // status: 1 or 0
    private InfractionCache _infractions;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new EventServerHandler object.
     *
     * @param  infractions  DOCUMENT ME!
     */
    public EventServerHandler(InfractionCache infractions) {
        _infractions = infractions;
    }

    //~ Methods **************************************************************************************************************************************

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, IOException { // (2)

        ByteBuf buf = (ByteBuf) msg;
        Infraction infraction = new ObjectMapper().readValue(buf.toString(CharsetUtil.UTF_8), Infraction.class);

        _infractions.put(infraction);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
