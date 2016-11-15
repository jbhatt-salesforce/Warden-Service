package com.salesforce.dva.warden.client;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import org.slf4j.LoggerFactory;

/**
 * Created by jbhatt on 10/12/16.
 */
class MetricUpdater extends  Thread {

    Map<String, Double> _values;
    WardenService _wardenService;

    MetricUpdater(Map<String, Double> values, WardenService wardenService) {
        this._values = values;
        this._wardenService = wardenService;
    }

    /**
     * re-evaluate this to do a batch metric update for multiple user-ids and policy ids. psudo code: put this whole thing in a while (!inturrupted)
     * loop get the current timestamp and truncate it to a whole min copy the results from values to a new Map (temporary) to hold a shorter lock on
     * the values map do a bulk update to the server For testing: write the values to the client and read it back from the server to verify this
     * class. Mock it for now.
     */
    @Override
    public void run() {
        long delta = 0;
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(60000-delta);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                continue;
            }

            Long start = System.currentTimeMillis();
            Long time = (start / 60000) * 60000;

            Map<String, Double> copyOfValues = new HashMap<>(_values);

            PolicyService policyService = _wardenService.getPolicyService();

            copyOfValues.forEach((String k, Double v) -> {
                List<String> items = Arrays.asList(k.split(":"));

                Map<Long, Double> metric = new HashMap<>();
                metric.put(time, v);
                try {
                    policyService.updateMetricsForUserAndPolicy(new BigInteger(items.get(0)), items.get(1), metric);
                } catch (IOException ex) {
                    LoggerFactory.getLogger(getClass()).warn("Failed to update metric.", ex);
                }
            });
            delta = System.currentTimeMillis()-start;
        }
        _values = null;
        _wardenService = null;
    }
}
