package org.leucam.telegram.bot.service.impl;

import feign.FeignException;
import org.apache.commons.io.FileUtils;
import org.leucam.telegram.bot.client.OrderResourceClient;
import org.leucam.telegram.bot.client.ProductResourceClient;
import org.leucam.telegram.bot.client.UserResourceClient;
import org.leucam.telegram.bot.dto.OrderDTO;
import org.leucam.telegram.bot.dto.ProductDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.model.Action;
import org.leucam.telegram.bot.model.type.ActionType;
import org.leucam.telegram.bot.polling.LeucamOrderBot;
import org.leucam.telegram.bot.repository.ActionRepository;
import org.leucam.telegram.bot.service.ResourceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class ResourceManagerServiceImpl implements ResourceManagerService {
    @Autowired
    LeucamOrderBot leucamOrderBot;

    @Value("${leucam.repository.path}")
    private String repositoryPath;

    @Value("${leucam.repository.extension}")
    private String repositoryExtension;

    @Autowired
    private UserResourceClient userResourceClient;

    @Autowired
    private OrderResourceClient orderResourceClient;

    @Autowired
    private ProductResourceClient productResourceClient;

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

    @Override
    public void postOrder(OrderDTO orderDTO) {
        if(ActionType.QUICK_PRINT.equals(orderDTO.getActionType())){
            ProductDTO productPersisted = productResourceClient.postProduct(orderDTO.getProduct());
            orderDTO.setProduct(productPersisted);
        }
        orderResourceClient.postOrder(orderDTO);
    }

    @Override
    public List<OrderDTO> getOrders(Integer user_id) {
        UserDTO userDTO = findUserByTelegramId(user_id);
        return orderResourceClient.findAllOrdersByUser(userDTO.getId());
    }

    @Override
    public OrderDTO getOrder(String call_data) {
        String[] split = call_data.split("#");
        Long orderId = Long.parseLong(split[1]);
        return orderResourceClient.findOrderById(orderId);
    }

    @Override
    public File saveDocument(Update update) {
        Document doc = update.getMessage().getDocument();
        StringBuffer stringBuffer = new StringBuffer(repositoryPath);
        stringBuffer.append(doc.getFileId());
        stringBuffer.append(repositoryExtension);
        File archived = new File(stringBuffer.toString());
        try {
            File original = downloadFileWithId(doc.getFileId());
            FileUtils.copyFile(original, archived);
        } catch (TelegramApiException | IOException e) {
        }
        return archived;
    }

    private File downloadFileWithId(String fileId) throws TelegramApiException {
        return leucamOrderBot.downloadFile(leucamOrderBot.execute(new GetFile().setFileId(fileId)));
    }
}
