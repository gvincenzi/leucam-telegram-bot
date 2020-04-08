package org.leucam.telegram.bot.polling;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.leucam.telegram.bot.dto.OrderDTO;
import org.leucam.telegram.bot.dto.ProductDTO;
import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.model.Action;
import org.leucam.telegram.bot.model.ActionType;
import org.leucam.telegram.bot.model.ColorType;
import org.leucam.telegram.bot.model.FrontBackType;
import org.leucam.telegram.bot.polling.factory.ItemFactory;
import org.leucam.telegram.bot.service.ResourceManagerService;
import org.leucam.telegram.bot.service.TelegramAdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;

@Component
public class LeucamOrderBot extends TelegramLongPollingBot {
    @Value("${leucam.telegram.bot.username}")
    private String botUsername;

    @Value("${leucam.telegram.bot.token}")
    private String botToken;

    @Autowired
    ResourceManagerService resourceManagerService;

    @Autowired
    TelegramAdministratorService telegramAdministratorService;

    @Autowired
    ItemFactory itemFactory;

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = null;
        Integer user_id = null;
        if (update.hasCallbackQuery()) {
            // Set variables
            String call_data = update.getCallbackQuery().getData();
            user_id = update.getCallbackQuery().getFrom().getId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            if (call_data.equals("iscrizione")) {
                message = itemFactory.message(chat_id, "Per iscriversi al sistema basta scrivere un messaggio in questa chat con solo la propria email.\nLeucam Print Manager vi iscriverà al sistema con i dati del vostro account Telegram e con la mail che avrete indicato");
            } else if (call_data.equals("cancellazione")) {
                resourceManagerService.deleteUser(user_id);
                message = itemFactory.message(chat_id, "Utente rimosso correttamente");
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
            }
        } else if (update.hasMessage()){
            user_id = update.getMessage().getFrom().getId();
            Long chat_id = update.getMessage().getChatId();
            Action actionInProgress = getActionInProgress(user_id);

            if (update.getMessage().getText() != null && update.getMessage().getText().equalsIgnoreCase("/start")) {
                message = itemFactory.welcomeMessage(update);
            } else if (update.getMessage().getDocument() != null && update.getMessage().getDocument().getMimeType().equalsIgnoreCase("application/pdf")) {
                Document doc = update.getMessage().getDocument();
                try {
                    File original = downloadFileWithId(doc.getFileId());
                    File copied = new File(original.getName());
                    FileUtils.copyFile(original, copied);
                } catch (TelegramApiException | IOException e) {
                }

                Action action = new Action();
                action.setActionType(ActionType.QUICK_PRINT);
                action.setTelegramUserId(update.getMessage().getFrom().getId());
                action.setName(doc.getFileName());
                action.setFileId(doc.getFileId());
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
        } catch (TelegramApiException e) {
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

    private File downloadFileWithId(String fileId) throws TelegramApiException {
        return this.downloadFile(this.execute(new GetFile().setFileId(fileId)));
    }
}
