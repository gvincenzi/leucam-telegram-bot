package org.leucam.telegram.bot.service;

import org.leucam.telegram.bot.dto.OrderDTO;
import org.leucam.telegram.bot.dto.ProductDTO;
import org.leucam.telegram.bot.dto.UserCreditDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.model.Action;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public interface ResourceManagerService {
    void deleteUser(Integer user_id);
    UserDTO findUserByTelegramId(Integer user_id);
    void addUser(User user, String mail);
    Action getActionInProgress(Integer telegramUserId);
    void saveAction(Action action);
    void postOrder(OrderDTO orderDTO);
    OrderDTO getOrder(String call_data);
    List<OrderDTO> getOrders(Integer user_id);
    File saveDocument(Update update);
    BigDecimal totalUserCredit();
    UserCreditDTO getCredit(Integer user_id);
    UserCreditDTO addCredit(Integer user_id, BigDecimal credit);
    String makePayment(OrderDTO orderDTO);
    void deleteOrder(OrderDTO orderDTO);
    List<ProductDTO> getProducts();
    ProductDTO getProduct(String call_data);
}
