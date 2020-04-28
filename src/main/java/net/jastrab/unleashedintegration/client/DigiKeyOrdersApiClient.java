package net.jastrab.unleashedintegration.client;

import net.jastrab.digikey.model.ordersupport.OrderStatus;
import net.jastrab.digikey.model.ordersupport.SalesorderHistoryItem;
import net.jastrab.unleashedintegration.configuration.FeignDigiKeyConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "digikey-orders",
        url = "https://api.digikey.com",
        path = "/OrderDetails/v3",
        configuration = FeignDigiKeyConfiguration.class
)
public interface DigiKeyOrdersApiClient {

    @GetMapping("/Status/{orderId}")
    OrderStatus getOrderDetails(@PathVariable("orderId") String orderId);

    /**
     * Params can include the following keys:
     * - StartDate : ISO Formatted date from which to pull order history
     * - EndDate : ISO Formatted date ending range for order history
     * - OpenOrders : Boolean flag to select only open orders
     * @param params
     * @return
     */
    @GetMapping("/History")
    List<SalesorderHistoryItem> getOrderHistory(@SpringQueryMap(encoded = true)Map<String, String> params);

}
