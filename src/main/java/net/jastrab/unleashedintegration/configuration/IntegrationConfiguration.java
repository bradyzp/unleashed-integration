package net.jastrab.unleashedintegration.configuration;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.digikey.model.ordersupport.OrderStatus;
import net.jastrab.digikey.model.ordersupport.SalesorderHistoryItem;
import net.jastrab.unleashed.api.GetSupplierRequest;
import net.jastrab.unleashed.api.models.Product;
import net.jastrab.unleashed.api.models.PurchaseOrder;
import net.jastrab.unleashed.api.models.PurchaseOrderLine;
import net.jastrab.unleashed.api.models.Supplier;
import net.jastrab.unleashedintegration.integration.DigiKeyOrderMessageSource;
import net.jastrab.unleashedintegration.model.DigiKeyProductWrapper;
import net.jastrab.unleashedintegration.repository.OrderStatusRepo;
import net.jastrab.unleashedintegration.repository.UnleashedItemsRepo;
import net.jastrab.unleashedintegration.service.DigiKeyOrderProcessor;
import net.jastrab.unleashedintegration.service.DigiKeyOrderService;
import net.jastrab.unleashedintegration.service.UnleashedMetaService;
import net.jastrab.unleashedspringclient.client.UnleashedClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.*;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableIntegration
@IntegrationComponentScan("net.jastrab.unleashedintegration.service")
public class IntegrationConfiguration {
    @Value("${integration.threads:6}")
    private Integer processingThreads = 6;

    @Value("${integration.interval-hours:4}")
    private Integer intervalHours = 4;

    private final DigiKeyOrderService orderService;
    private final OrderStatusRepo ordersRepository;
    private final UnleashedItemsRepo itemsRepository;
    private final UnleashedClient unleashedClient;
    private final UnleashedMetaService unleashedMetaService;

    private final PollerSpec sourcePoller;

    public IntegrationConfiguration(DigiKeyOrderService orderService,
                                    OrderStatusRepo ordersRepository,
                                    UnleashedItemsRepo itemsRepository,
                                    UnleashedClient unleashedClient,
                                    UnleashedMetaService unleashedMetaService) {
        this.orderService = orderService;
        this.ordersRepository = ordersRepository;
        this.itemsRepository = itemsRepository;
        this.unleashedClient = unleashedClient;
        this.unleashedMetaService = unleashedMetaService;

        this.sourcePoller = Pollers.fixedRate(Duration.ofHours(intervalHours));
    }

    @Bean
    public Executor getThreadPoolExecutor() {
        return Executors.newFixedThreadPool(processingThreads);
    }

    @Bean("orders.input")
    public MessageChannel orderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel discardedLineItemChannel() {
        return new DirectChannel();
    }

    /**
     * Integration Flow
     */

    @Bean
    public MessageSource<String> orderServiceMessageSource() {
        return new DigiKeyOrderMessageSource(
                this.orderService,
                orderId -> !ordersRepository.existsBySalesorderId(orderId),
                15);
    }

    @Bean
    public IntegrationFlow pollingInputFlow() {
        return IntegrationFlows.from(orderServiceMessageSource(), spec -> spec.poller(this.sourcePoller))
                .channel(orderInputChannel())
                .get();
    }

    @Bean
    public IntegrationFlow digiKeyUnleashedFlow() {
        return IntegrationFlows.from(orderInputChannel())
                .filter(String.class, it -> !ordersRepository.existsBySalesorderId(it))
                .handle(String.class, (payload, headers) -> {
                    log.info("Fetching detailed order status for order id: {}", payload);
                    return orderService.getDetailedOrderStatus(payload);
                })
                .handle(OrderStatus.class, (payload, headers) -> ordersRepository.save(payload))
                .filter(OrderStatus.class, orderStatus -> (orderStatus.getPurchaseOrder() != null && orderStatus.getPurchaseOrder().isEmpty()))
                .enrichHeaders(h -> h.messageProcessor(m -> {
                    return new MessageHeaders(Map.of("SalesOrderId", ((OrderStatus) m.getPayload()).getSalesorderId()));
                }))
                .split(OrderStatus.class, this::splitOrderStatus)
                .channel(MessageChannels.executor(getThreadPoolExecutor()))
                .handle(DigiKeyProductWrapper.class, (payload, headers) -> createUnleashedProduct(payload))
                .aggregate()
                .<List<PurchaseOrderLine>>handle(((payload, headers) -> {
                    final String salesOrderId = headers.get("SalesOrderId", String.class);
                    final PurchaseOrder order = generatePurchaseOrder(salesOrderId, payload);

                    log.info("Preparing to create purchase order: {}", order);
                    unleashedClient.createItem(order, PurchaseOrder.class)
                            .ifPresentOrElse(purchaseOrder -> log.info("Created purchase order: {}", purchaseOrder),
                                    () -> log.warn("Failed to create purchase order"));

                    return null;
                })).get();
    }

    private OrderStatus orderStatusFromHistory(String salesOrderId) {
        log.info("Fetching order details for Sales Order ID: {}", salesOrderId);
        return orderService.getDetailedOrderStatus(salesOrderId);
    }

    private List<DigiKeyProductWrapper> splitOrderStatus(OrderStatus orderStatus) {
        return orderStatus.getLineItems().stream()
                .map(item -> new DigiKeyProductWrapper(orderStatus.getSalesorderId(), item))
                .collect(Collectors.toList());
    }

    private PurchaseOrderLine createUnleashedProduct(DigiKeyProductWrapper wrapper) {
        final Supplier digiKey = unleashedClient.getSupplier("DIGI").orElseThrow();
        final Product unleashedProduct = wrapper.toUnleashedProduct(digiKey);
        unleashedProduct.setUnitOfMeasure(unleashedMetaService.getDefaultUnitOfMeasure());

        Optional<Product> created = unleashedClient.upsertProduct(wrapper.toUnleashedProduct(digiKey), true);
        if (!itemsRepository.existsByProductCode(wrapper.getProductCode())) {
            log.info("Inserting record into itemsRepository");
            itemsRepository.save(unleashedProduct);
        }
        return created
                .map(wrapper::toPurchaseOrderLine)
                .orElseThrow(() -> new RuntimeException("Failed to create Unleashed Product"));
    }

    private PurchaseOrder generatePurchaseOrder(String salesOrderId, List<PurchaseOrderLine> orderLines) {
        final GetSupplierRequest gsr = GetSupplierRequest.builder().supplierCode("DIGI").build();
        final Supplier digikey = unleashedClient.getItem(gsr).orElseThrow();
        final PurchaseOrder order = new PurchaseOrder(digikey);
        order.setSupplierRef(salesOrderId);
        order.setPurchaseOrderLines(orderLines);
        order.setComments("Created from DigiKey order: " + salesOrderId);

        return order;
    }

}
