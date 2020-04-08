package org.leucam.telegram.bot.listener;

import org.leucam.telegram.bot.binding.MQBinding;
import org.leucam.telegram.bot.dto.OrderDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.service.TelegramAdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@EnableBinding(MQBinding.class)
public class MQListener {
    @Autowired
    TelegramAdministratorService telegramAdministratorService;

    @StreamListener(target = MQBinding.USER_REGISTRATION)
    public void processUserRegistration(UserDTO msg) throws TelegramApiException {
        telegramAdministratorService.sendRegistrationMessage(msg);
    }

    @StreamListener(target = MQBinding.USER_ORDER)
    public void processUserOrder(OrderDTO msg) throws TelegramApiException {
        telegramAdministratorService.sendOrderMessage(msg);
    }
}