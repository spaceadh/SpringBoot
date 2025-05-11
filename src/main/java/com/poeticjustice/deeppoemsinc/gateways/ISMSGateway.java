package com.poeticjustice.deeppoemsinc.gateways;
import java.util.concurrent.CompletableFuture;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;

public interface ISMSGateway {
    CompletableFuture<Boolean> sendSMSAsync(QueuedSMS notification);
}
