package net.jastrab.unleashedintegration.model;

import net.jastrab.digikey.model.ordersupport.OrderStatus;

public class OrderDetails {
    private final OrderStatus orderStatus;
    private final String dateEntered;

    public OrderDetails(OrderStatus orderStatus, String dateEntered) {
        this.orderStatus = orderStatus;
        this.dateEntered = dateEntered;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public String getDateEntered() {
        return dateEntered;
    }
}
