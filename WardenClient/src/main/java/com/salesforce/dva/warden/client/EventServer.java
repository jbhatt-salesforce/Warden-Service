package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.dto.Infraction;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jbhatt on 11/21/16.
 */
public class EventServer extends ChannelInboundHandlerAdapter {

    public EventServer() {
    }

    public void start(int port, Map<String, Infraction> infractions) throws SocketException {
        //start listening
    }

    public void stop() {
        //stop listening
    }

    public void handleEvent(){
        //A filter will call handleEvent method and send a response to the warden server
        //update infraction cache
        //response json
        //status: 1 or 0
    }
}
