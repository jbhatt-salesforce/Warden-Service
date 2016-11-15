package com.salesforce.dva.warden.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dva.warden.dto.Infraction;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

/**
 * Created by jbhatt on 10/12/16.
 */
class EventListener extends Thread {

    Map<String, Infraction> _infractions;
    WardenService _wardenService;
    int _port;
    DatagramSocket _socket;

    EventListener(Map<String, Infraction> infractions, WardenService wardenService, int port) throws SocketException {
        this._infractions = infractions;
        this._wardenService = wardenService;
        this._port = port;
        this._socket = new DatagramSocket(_port);
    }

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
        _socket.close();
    }
    
    

    @Override
    public void run() {
        long delta = 0;
        while (!Thread.interrupted()) {
            Thread.yield();
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                _socket.receive(packet);
                ObjectMapper mapper = new ObjectMapper();
                Infraction infraction = mapper.readValue(packet.getData(), Infraction.class);
                _infractions.put(DefaultWardenClient._createKey(infraction.getPolicyId(),infraction.getUserName()), infraction);
            } catch (IOException e) {
                interrupt();
                throw new RuntimeException(e);
            }
        }
        _infractions = null;
        _wardenService = null;
        _socket = null;
    }
}
