package org.leucam.telegram.bot.client;

import org.leucam.telegram.bot.configuration.FeignClientConfiguration;
import org.leucam.telegram.bot.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "leucam-order-service/orders", configuration = FeignClientConfiguration.class)
public interface OrderResourceClient {
    @GetMapping("/users/{id}")
    List<OrderDTO> findAllOrdersByUser(@PathVariable Long id);

    @GetMapping("/{id}")
    OrderDTO findOrderById(@PathVariable Long id);

    @PostMapping("/telegram")
    OrderDTO postOrder(@RequestBody OrderDTO order);

    @DeleteMapping("/{id}")
    void deleteOrder(@PathVariable("id") Long id);
}
