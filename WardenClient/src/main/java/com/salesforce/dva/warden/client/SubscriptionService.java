package com.salesforce.dva.warden.client;

import com.salesforce.dva.warden.dto.Subscription;
import com.salesforce.dva.warden.client.WardenService.EndpointService;
import java.io.IOException;

final class SubscriptionService extends EndpointService {
    private static final String REQUESTURL = "/subscription";
    
    SubscriptionService(WardenHttpClient client) {
        super(client);
    }

    WardenResponse<Subscription> subscribe(Subscription subscription) throws IOException {
        String requestUrl = REQUESTURL;
        return getClient().executeHttpRequest(WardenHttpClient.RequestType.POST, requestUrl, subscription);
    }
    
    WardenResponse<Subscription> unsubscribe(Subscription subscription) throws IOException {
        String requestUrl = REQUESTURL + "/" + subscription.getId().toString();
        return getClient().executeHttpRequest(WardenHttpClient.RequestType.DELETE, requestUrl,null);
    }
    
}
