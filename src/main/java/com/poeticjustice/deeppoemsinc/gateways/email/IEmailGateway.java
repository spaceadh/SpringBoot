package com.poeticjustice.deeppoemsinc.gateways.email;

import com.poeticjustice.deeppoemsinc.models.mongo.QueuedEmail;
import java.util.concurrent.CompletableFuture;

public interface IEmailGateway {
    CompletableFuture<Boolean> sendEmailAsync(QueuedEmail email);
}