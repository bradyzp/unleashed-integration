package net.jastrab.unleashedintegration.service;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.digikey.model.ordersupport.OrderStatus;
import net.jastrab.digikey.model.ordersupport.SalesorderHistoryItem;
import net.jastrab.unleashedintegration.client.DigiKeyOrdersApiClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DigiKeyOrderService {
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DigiKeyOrdersApiClient client;

    public DigiKeyOrderService(DigiKeyOrdersApiClient client) {
        this.client = client;
    }

    public List<SalesorderHistoryItem> getOrderHistory(LocalDate startDate) {
        log.info("Fetching order history from DigiKey");
        final Map<String, String> params = Collections.singletonMap("StartDate", ISO_DATE_FORMATTER.format(startDate));

        List<SalesorderHistoryItem> value = client.getOrderHistory(params);
        log.info("Got response from digikey: {}", value.stream().map(SalesorderHistoryItem::getSalesorderId).collect(Collectors.joining(", ")));

        return value;
    }

    public OrderStatus getDetailedOrderStatus(String salesOrderId) {
        log.info("Getting OrderStatus for SalesOrderId: {}", salesOrderId);
        return client.getOrderDetails(salesOrderId);
    }



}
