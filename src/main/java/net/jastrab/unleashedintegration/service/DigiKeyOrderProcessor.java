package net.jastrab.unleashedintegration.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface DigiKeyOrderProcessor {

    @Gateway(requestChannel = "orders.input")
    void processOrder(String salesOrderId);

}
