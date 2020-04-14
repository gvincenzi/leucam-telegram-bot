package org.leucam.telegram.bot.service.impl;

import org.leucam.telegram.bot.client.UserResourceClient;
import org.leucam.telegram.bot.dto.OrderDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.polling.LeucamOrderBot;
import org.leucam.telegram.bot.service.TelegramAdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
public class TelegramAdministratorServiceImpl implements TelegramAdministratorService {
    @Autowired
    LeucamOrderBot leucamOrderBot;

    @Autowired
    UserResourceClient userResourceClient;

    @Override
    public void sendRegistrationMessage(UserDTO userDTO) throws TelegramApiException {
        List<UserDTO> administrators = userResourceClient.getAdministrators();
        if(administrators != null && !administrators.isEmpty()) {
            for(UserDTO administrator : administrators) {
                SendMessage message = new SendMessage()
                        .setChatId(String.valueOf(administrator.getTelegramUserId()))
                        .setText("Nuovo utente registrato : " + userDTO.getName() + " " + userDTO.getSurname());
                leucamOrderBot.execute(message);
            }
        }
    }

    @Override
    public void sendOrderMessage(OrderDTO orderDTO) throws TelegramApiException {
        List<UserDTO> administrators = userResourceClient.getAdministrators();
        if(administrators != null && !administrators.isEmpty()) {
            for(UserDTO administrator : administrators) {
                SendMessage message = new SendMessage()
                        .setChatId(String.valueOf(administrator.getTelegramUserId()))
                        .setText("Nuovo ordine registrato da "+orderDTO.getUser().getName()+" "+orderDTO.getUser().getSurname()+":\n" + orderDTO.toString());
                leucamOrderBot.execute(message);
            }
        }
    }

    @Override
    public void sendOrderUpdateMessage(OrderDTO msg) throws TelegramApiException {
        UserDTO destination = userResourceClient.findUserByTelegramId(msg.getUser().getTelegramUserId());
        if(destination != null) {
            SendMessage message = new SendMessage()
                    .setChatId(String.valueOf(destination.getTelegramUserId()))
                    .setText("Aggiornamento del tuo ordine #"+msg.getOrderId() + ":\n" + msg.toString());
            leucamOrderBot.execute(message);
        }
    }
}
