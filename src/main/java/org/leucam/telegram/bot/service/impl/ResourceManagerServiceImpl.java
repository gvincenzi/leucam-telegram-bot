package org.leucam.telegram.bot.service.impl;

import feign.FeignException;
import org.leucam.telegram.bot.client.UserResourceClient;
import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.model.Action;
import org.leucam.telegram.bot.repository.ActionRepository;
import org.leucam.telegram.bot.service.ResourceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ResourceManagerServiceImpl implements ResourceManagerService {
    @Autowired
    private UserResourceClient userResourceClient;

    @Autowired
    private ActionRepository actionRepository;

    public UserDTO findUserByTelegramId(Integer user_id) {
        UserDTO user;
        try {
            user = userResourceClient.findUserByTelegramId(user_id);
        } catch (FeignException ex) {
            user = null;
        }
        return user;
    }

    @Override
    public void addUser(User from, String mail) {
        UserDTO userDTO = new UserDTO();
        userDTO.setTelegramUserId(from.getId());
        userDTO.setName(from.getFirstName());
        userDTO.setSurname(from.getLastName());
        userDTO.setMail(mail);
        userDTO.setAdministrator(Boolean.FALSE);
        userResourceClient.addUser(userDTO);
    }

    @Override
    public void deleteUser(Integer user_id) {
        userResourceClient.deleteUser(user_id);
    }

    @Override
    public Action getActionInProgress(Integer telegramUserId){
        Optional<Action> actionOptional = actionRepository.findByTelegramUserIdAndInProgressTrue(telegramUserId);
        if(actionOptional.isPresent()){
            return actionOptional.get();
        } else {
            return null;
        }
    }

    @Override
    public void saveAction(Action action) {
        actionRepository.save(action);
    }
}
