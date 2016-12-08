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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

import static com.salesforce.dva.warden.client.DefaultWardenClient.requireThat;

/**
 * Event handler used by the event server to update the infraction cache with newly received events.
 *
 * @author  Jigna Bhatt (jbhatt@salesforce.com)
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class EventServerHandler extends ChannelInboundHandlerAdapter {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final Logger LOGGER = LoggerFactory.getLogger(EventServerHandler.class);

    //~ Instance fields ******************************************************************************************************************************

    private final InfractionCache _infractions;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new EventServerHandler object.
     *
     * @param  infractions  The infraction cache to update with newly received events. Must be thread safe.
     */
    public EventServerHandler(InfractionCache infractions) {
        requireThat(infractions != null, "The infraction cache cannot be null.");
        _infractions = infractions;
    }

    //~ Methods **************************************************************************************************************************************

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, IOException {
        ByteBuf buf = (ByteBuf) msg;
        Infraction infraction = new ObjectMapper().readValue(buf.toString(CharsetUtil.UTF_8), Infraction.class);

        _infractions.put(infraction);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("An error occurred processing the incoming event.", cause);
        ctx.close();
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
