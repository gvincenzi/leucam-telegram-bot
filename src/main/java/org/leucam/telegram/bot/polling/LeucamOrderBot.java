package org.leucam.telegram.bot.polling;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.leucam.telegram.bot.dto.OrderDTO;
import org.leucam.telegram.bot.dto.ProductDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.model.Action;
import org.leucam.telegram.bot.model.type.ActionType;
import org.leucam.telegram.bot.model.type.ColorType;
import org.leucam.telegram.bot.model.type.FrontBackType;
import org.leucam.telegram.bot.polling.factory.ItemFactory;
import org.leucam.telegram.bot.service.ResourceManagerService;
import org.leucam.telegram.bot.service.TelegramAdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LeucamOrderBot extends TelegramLongPollingBot {
    @Value("${leucam.telegram.bot.username}")
    private String botUsername;

    @Value("${leucam.telegram.bot.token}")
    private String botToken;

    @Value("${leucam.telegram.bot.stripe.token}")
    private String stripeToken;

    @Value("${leucam.template.paymentInternalCreditURL}")
    public String templatePaymentInternalCreditURL;

    @Autowired
    ResourceManagerService resourceManagerService;

    @Autowired
    TelegramAdministratorService telegramAdministratorService;

    @Autowired
    ItemFactory itemFactory;

    @Override
    public void onUpdateReceived(Update update) {
        BotApiMethod message = null;
        Integer user_id = null;

        if(update.hasPreCheckoutQuery()){
            /* CHECK PAYLOAD */
            message = new AnswerPreCheckoutQuery();
            ((AnswerPreCheckoutQuery)message).setOk(true);
            ((AnswerPreCheckoutQuery)message).setPreCheckoutQueryId(update.getPreCheckoutQuery().getId());
        }
        if (update.hasCallbackQuery()) {
            // Set variables
            String call_data = update.getCallbackQuery().getData();
            user_id = update.getCallbackQuery().getFrom().getId();
            Long chat_id = update.getCallbackQuery().getMessage().getChatId();

            if (call_data.equals("iscrizione")) {
                message = itemFactory.message(chat_id, "Per iscriversi al sistema basta scrivere un messaggio in questa chat con solo la propria email.\nLeucam Print Manager vi iscriverà al sistema con i dati del vostro account Telegram e con la mail che avrete indicato");
            } else if (call_data.equals("cancellazione")) {
                resourceManagerService.deleteUser(user_id);
                message = itemFactory.message(chat_id, "Utente rimosso correttamente");
            } else if (call_data.equals("creditoResiduo")) {
                message = itemFactory.message(chat_id,String.format("Il tuo credito residuo : %s €", resourceManagerService.getCredit(user_id).getCredit()));
            } else if (call_data.equals("fondoCassa")) {
                message = itemFactory.message(chat_id,"Fondo cassa corrente : " + resourceManagerService.totalUserCredit() + "€");
            } else if (call_data.equals("ricaricaCredito")) {
                message = itemFactory.credit(chat_id);
            } else if(call_data.startsWith("credit#")) {
                String choice = call_data.substring(call_data.indexOf("#") + 1);
                StringBuilder payload = new StringBuilder();
                payload.append(user_id);
                payload.append(choice);
                LabeledPrice price = new LabeledPrice();
                price.setLabel("Ricarica credito");
                price.setAmount(Integer.parseInt(choice));

                message = new SendInvoice();
                ((SendInvoice) message).setProviderToken(stripeToken);
                List<LabeledPrice> prices = new ArrayList<>();
                prices.add(price);
                ((SendInvoice) message).setPrices(prices);
                ((SendInvoice) message).setTitle("Leucam Print Manager - Credito");
                ((SendInvoice) message).setDescription("Ricarica del conto prepagato");
                ((SendInvoice) message).setCurrency("EUR");
                ((SendInvoice) message).setChatId(chat_id.intValue());
                ((SendInvoice) message).setPayload(payload.toString());
                ((SendInvoice) message).setStartParameter("pay");

            } else if(call_data.equals("stampaImmediata")) {
                message = itemFactory.message(chat_id, "Ordina una Stampa Immediata in solo 5 passi:\n1- Carica un file PDF (20 MB max)\n2- Indica se vuoi una stampa a colori o in bianco e nero\n3- Indica se preferisci una stampa fronte o fronte/retro\n4- Indica quante pagine per foglio (1, 2 o 4) preferisci\n5- Indica il numero di copie desiderate\n\nSe hai tutte le informazioni necessarie inizia l'ordine caricando ora il file PDF in questa finestra di chat.");
            } else if(call_data.startsWith("printParametersBackFont#")){
                String choice = call_data.substring(call_data.indexOf("#")+1);
                Action actionInProgress = getActionInProgress(user_id);
                actionInProgress.setFrontBackType(FrontBackType.valueOf(choice));
                resourceManagerService.saveAction(actionInProgress);
                message = itemFactory.printParametersSelection(actionInProgress,chat_id);
            } else if(call_data.startsWith("printParametersPagesPerSheet#")){
                String choice = call_data.substring(call_data.indexOf("#")+1);
                Action actionInProgress = getActionInProgress(user_id);
                actionInProgress.setPagesPerSheet(Integer.parseInt(choice));
                resourceManagerService.saveAction(actionInProgress);
                message = itemFactory.printParametersSelection(actionInProgress,chat_id);
            } else if(call_data.startsWith("printParametersGrayScaleOrColor#")){
                String choice = call_data.substring(call_data.indexOf("#")+1);
                Action actionInProgress = getActionInProgress(user_id);
                actionInProgress.setColorType(ColorType.valueOf(choice));
                resourceManagerService.saveAction(actionInProgress);
                message = itemFactory.printParametersSelection(actionInProgress,chat_id);
            } else if (call_data.startsWith("listaOrdini")) {
                List<OrderDTO> orders = resourceManagerService.getOrders(user_id);
                if (orders.isEmpty()) {
                    message = itemFactory.message(chat_id,"Non hai ordini in corso");
                } else {
                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    Collections.sort(orders);
                    for (OrderDTO orderDTO : orders) {
                        List<InlineKeyboardButton> rowInline = new ArrayList<>();
                        rowInline.add(new InlineKeyboardButton().setText("ID#"+orderDTO.getOrderId()+" : "+orderDTO.getProduct().getName()).setCallbackData("orderDetails#" + orderDTO.getOrderId()));
                        rowsInline.add(rowInline);
                    }

                    markupInline.setKeyboard(rowsInline);
                    message = itemFactory.message(chat_id,"Qui di seguito la lista dei tuoi ordini in corso, per accedere ai dettagli cliccare sull'ordine:\n");

                    ((SendMessage)message).setReplyMarkup(markupInline);
                }
            } else if (call_data.startsWith("orderDetails#")) {
                OrderDTO orderDTO = resourceManagerService.getOrder(call_data);
                message = itemFactory.message(chat_id,orderDTO.toString());
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
                List<InlineKeyboardButton> rowInline2 = new ArrayList<>();

                String paymentInternalCreditURL = String.format(templatePaymentInternalCreditURL,orderDTO.getOrderId()).replaceAll(" ","%20");
                rowInline1.add(new InlineKeyboardButton().setText("Paga questo ordine").setUrl(paymentInternalCreditURL));
                rowInline2.add(new InlineKeyboardButton().setText("Torna alla lista").setCallbackData("listaOrdini"));
                // Set the keyboard to the markup
                if(!orderDTO.getPaid()){
                    rowsInline.add(rowInline1);
                }

                rowsInline.add(rowInline2);
                // Add it to the message
                markupInline.setKeyboard(rowsInline);
                ((SendMessage)message).setReplyMarkup(markupInline);
            }
        } else if (update.hasMessage()){
            user_id = update.getMessage().getFrom().getId();
            Long chat_id = update.getMessage().getChatId();
            Action actionInProgress = getActionInProgress(user_id);

            if (update.getMessage().getText() != null && update.getMessage().getText().equalsIgnoreCase("/start")) {
                message = itemFactory.welcomeMessage(update);
            } else if (update.getMessage().getDocument() != null && update.getMessage().getDocument().getMimeType().equalsIgnoreCase("application/pdf")) {
                Document doc = update.getMessage().getDocument();
                File file = resourceManagerService.saveDocument(update);

                Action action = new Action();
                action.setActionType(ActionType.QUICK_PRINT);
                action.setTelegramUserId(update.getMessage().getFrom().getId());
                action.setName(doc.getFileName());
                action.setFileId(doc.getFileId());
                action.setFilePath(file.getAbsolutePath());
                resourceManagerService.saveAction(action);
                message = itemFactory.printParametersSelection(action,chat_id);
            } else if (update.getMessage().getText().contains("@")) {
                resourceManagerService.addUser(update.getMessage().getFrom(), update.getMessage().getText());
                message = itemFactory.message(chat_id, "Nuovo utente iscritto correttamente : una mail di conferma è stata inviata all'indirizzo specificato.\nClicca su /start per iniziare.");
            } else if(actionInProgress != null && actionInProgress.getNumberOfCopies() == null && StringUtils.isNumeric(update.getMessage().getText())){
                actionInProgress.setNumberOfCopies(Integer.parseInt(update.getMessage().getText()));
                actionInProgress.setInProgress(Boolean.FALSE);
                resourceManagerService.saveAction(actionInProgress);

                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setActionType(actionInProgress.getActionType());
                orderDTO.setColorType(actionInProgress.getColorType());
                orderDTO.setFrontBackType(actionInProgress.getFrontBackType());
                orderDTO.setNumberOfCopies(actionInProgress.getNumberOfCopies());
                orderDTO.setPagesPerSheet(actionInProgress.getPagesPerSheet());
                UserDTO userDTO = new UserDTO();
                userDTO.setTelegramUserId(user_id);
                orderDTO.setUser(userDTO);

                ProductDTO productDTO = new ProductDTO();
                /*productDTO.setProductId(actionInProgress.getProductId());*/
                if(ActionType.QUICK_PRINT.equals(actionInProgress.getActionType())){
                    productDTO.setActive(Boolean.FALSE);
                    productDTO.setDescription("Documento proposto da un utente");
                    productDTO.setName(actionInProgress.getName());
                    productDTO.setFileId(actionInProgress.getFileId());
                    productDTO.setFilePath(actionInProgress.getFilePath());
                }
                orderDTO.setProduct(productDTO);

                resourceManagerService.postOrder(orderDTO);
                message = itemFactory.message(chat_id, actionInProgress.toString()+"\n\nOrdine di stampa registrato correttamente, una mail di conferma con una sintesi dell'acquisto e le modalità di pagamento è stata inviata sul tuo indirizzo email.");

            } else if (update.hasMessage()) {
                message = itemFactory.welcomeMessage(update);
            }
        }

        try {
            execute(message); // Call method to send the message

            if(message instanceof AnswerPreCheckoutQuery){
                resourceManagerService.addCredit(update.getPreCheckoutQuery().getFrom().getId(),BigDecimal.valueOf(update.getPreCheckoutQuery().getTotalAmount()));
            }
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    private Action getActionInProgress(Integer user_id) {
        return resourceManagerService.getActionInProgress(user_id);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
