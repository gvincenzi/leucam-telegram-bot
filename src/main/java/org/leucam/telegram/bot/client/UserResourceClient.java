package org.leucam.telegram.bot.client;

import org.leucam.telegram.bot.configuration.FeignClientConfiguration;
import org.leucam.telegram.bot.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "leucam-order-service/users", configuration = FeignClientConfiguration.class)
public interface UserResourceClient {
    @GetMapping("/telegram/{id}")
    UserDTO findUserByTelegramId(@PathVariable("id") Integer id);

    @PostMapping()
    UserDTO addUser(@RequestBody UserDTO userDTO);

    @DeleteMapping("/telegram/{id}")
    Boolean deleteUser(@PathVariable("id") Integer id);

    @GetMapping("/administrator")
    List<UserDTO> getAdministrators();

    @GetMapping
    List<UserDTO> getUsers();
}
