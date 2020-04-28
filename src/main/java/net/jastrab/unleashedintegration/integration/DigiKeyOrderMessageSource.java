package net.jastrab.unleashedintegration.integration;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.digikey.model.ordersupport.SalesorderHistoryItem;
import net.jastrab.unleashedintegration.service.DigiKeyOrderService;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;

@Slf4j
public class DigiKeyOrderMessageSource extends AbstractMessageSource<String> {
    private final DigiKeyOrderService orderService;
    private final Predicate<String> filter;

    private final Queue<String> salesOrderIdQueue;

    public DigiKeyOrderMessageSource(DigiKeyOrderService orderService, Predicate<String> filter, int initialLookbackDays) {
        this.orderService = orderService;
        this.filter = filter;
        this.salesOrderIdQueue = new PriorityBlockingQueue<>(5);

        this.scanSalesOrders(initialLookbackDays);
    }

    private void scanSalesOrders(int lookbackDays) {
        log.info("Scanning for new DigiKey Sales Orders");
        this.orderService.getOrderHistory(LocalDate.now().minusDays(lookbackDays)).stream()
                .map(SalesorderHistoryItem::getSalesorderId)
                .filter(this.filter)
                .forEach(this.salesOrderIdQueue::add);
    }

    private void scanSalesOrders() {
        scanSalesOrders(1);
    }

    @Override
    protected AbstractIntegrationMessageBuilder<String> doReceive() {
        log.info("Checking queue for items to receive");
        if (this.salesOrderIdQueue.isEmpty()) {
            scanSalesOrders();
        }

        return Optional.ofNullable(this.salesOrderIdQueue.poll())
                .map(salesOrderId -> getMessageBuilderFactory()
                        .withPayload(salesOrderId)
                        .setHeader("SalesOrderId", salesOrderId))
                .orElse(null);
    }

    @Override
    public String getComponentType() {
        return "sales-order:inbound-channel-adapter";
    }
}
