package net.jastrab.unleashedintegration.controller;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.unleashedintegration.service.DigiKeyOrderProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/integration/digikey")
public class DigiKeyIntegrationController {
    private final DigiKeyOrderProcessor orderProcessor;

    @Autowired
    public DigiKeyIntegrationController(DigiKeyOrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @PostMapping("/{orderId}")
    public String processOrder(@PathVariable("orderId") String orderId) {
        log.info("Submitting order {} to order processing gateway", orderId);
        orderProcessor.processOrder(orderId);

        return "OK";
    }


}
