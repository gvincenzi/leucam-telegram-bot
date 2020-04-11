package org.leucam.telegram.bot.client;

import org.leucam.telegram.bot.dto.UserCreditDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient("leucam-payment-service/internal-credit")
public interface UserCreditResourceClient {
    @GetMapping("/{userId}")
    UserCreditDTO findById(@PathVariable("userId") Long userId);

    @PostMapping("/{additionalCredit}")
    UserCreditDTO newCredit(@RequestBody UserDTO userDTO, @PathVariable("additionalCredit") BigDecimal additionalCredit);

    @GetMapping("/totalUserCredit")
    BigDecimal totalUserCredit();

    @PutMapping("/{credit}")
    UserCreditDTO addCredit(@RequestBody UserDTO user, @PathVariable("credit") BigDecimal credit);

    @GetMapping("/order/{orderId}/price")
    BigDecimal getOrderPrice(@PathVariable("orderId") Long orderId);

    @PutMapping(value = "/order/{orderId}/pay")
    String makePayment(@PathVariable("orderId") Long orderId);
}
