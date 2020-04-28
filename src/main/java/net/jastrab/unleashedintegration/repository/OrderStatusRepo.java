package net.jastrab.unleashedintegration.repository;

import net.jastrab.digikey.model.ordersupport.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderStatusRepo extends MongoRepository<OrderStatus, String> {
    boolean existsBySalesorderId(String salesOrderId);
}
