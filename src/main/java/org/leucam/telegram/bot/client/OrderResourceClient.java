package org.leucam.telegram.bot.client;

import org.leucam.telegram.bot.configuration.FeignClientConfiguration;
import org.leucam.telegram.bot.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "leucam-order-service/orders", configuration = FeignClientConfiguration.class)
public interface OrderResourceClient {
    @GetMapping("/users/{id}")
    List<OrderDTO> findAllOrdersByUser(@PathVariable Long id);

    @GetMapping("/{id}")
    OrderDTO findOrderById(@PathVariable Long id);

    @PostMapping("/telegram")
    OrderDTO postOrder(@RequestBody OrderDTO order);
}
