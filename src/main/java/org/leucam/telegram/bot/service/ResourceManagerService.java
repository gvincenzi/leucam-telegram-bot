package org.leucam.telegram.bot.service;

import org.leucam.telegram.bot.dto.OrderDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.model.Action;
import org.telegram.telegrambots.meta.api.objects.User;

public interface ResourceManagerService {
    void deleteUser(Integer user_id);
    UserDTO findUserByTelegramId(Integer user_id);
    void addUser(User user, String mail);
    Action getActionInProgress(Integer telegramUserId);
    void saveAction(Action action);
    void postOrder(OrderDTO orderDTO);
}
