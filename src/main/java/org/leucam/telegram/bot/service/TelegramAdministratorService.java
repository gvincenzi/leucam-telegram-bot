package org.leucam.telegram.bot.service;

import org.leucam.telegram.bot.dto.OrderDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramAdministratorService {
    void sendRegistrationMessage(UserDTO userDTO) throws TelegramApiException;
    void sendOrderMessage(OrderDTO orderDTO) throws TelegramApiException;
}
