package net.jastrab.unleashedintegration.integration;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.digikey.model.ordersupport.SalesorderHistoryItem;
import net.jastrab.unleashedintegration.service.DigiKeyOrderService;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@Slf4j
public class DigiKeyOrderMessageSource extends AbstractMessageSource<String> implements SmartLifecycle {
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final Queue<String> salesOrderIdQueue;

    private final DigiKeyOrderService orderService;

    private Predicate<SalesorderHistoryItem> filter = value -> true;

    private Period maxOrderAge = Period.ofDays(5);

    public DigiKeyOrderMessageSource(DigiKeyOrderService orderService) {
        this.orderService = orderService;
        this.salesOrderIdQueue = new PriorityBlockingQueue<>(5);
    }

    private void scanSalesOrders(Period maxOrderAge) {
        log.info("Scanning for new DigiKey Sales Orders");
        this.orderService.getOrderHistory(LocalDate.now().minus(maxOrderAge)).stream()
                .filter(this.filter)
                .map(SalesorderHistoryItem::getSalesorderId)
                .forEach(this.salesOrderIdQueue::add);
    }

    private void scanSalesOrders() {
        scanSalesOrders(Period.ofDays(1));
    }

    public Predicate<SalesorderHistoryItem> getFilter() {
        return filter;
    }

    public void setFilter(Predicate<SalesorderHistoryItem> filter) {
        this.filter = filter;
    }

    public Period getMaxOrderAge() {
        return maxOrderAge;
    }

    public void setMaxOrderAge(Period maxOrderAge) {
        this.maxOrderAge = maxOrderAge;
    }

    @Override
    protected AbstractIntegrationMessageBuilder<String> doReceive() {
        log.info("Checking queue for items to receive");
        if (this.salesOrderIdQueue.isEmpty()) {
            log.info("Sales order queue is empty, checking for new items");
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

    @Override
    public void start() {
        if (!this.running.getAndSet(true)) {
            log.info("Starting DigiKeyOrderMessageSource");
            this.scanSalesOrders(maxOrderAge);
        }
    }

    @Override
    public void stop() {
        if (this.running.getAndSet(false)) {
            log.info("Stopping DigiKeyOrderMessageSource");
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }
}
