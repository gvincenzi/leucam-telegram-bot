package org.leucam.telegram.bot.polling.factory.impl;

import org.leucam.telegram.bot.dto.UserDTO;
import org.leucam.telegram.bot.model.Action;
import org.leucam.telegram.bot.model.ColorType;
import org.leucam.telegram.bot.model.FrontBackType;
import org.leucam.telegram.bot.polling.factory.ItemFactory;
import org.leucam.telegram.bot.service.ResourceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemFactoryImpl implements ItemFactory {
    @Autowired
    ResourceManagerService resourceManagerService;

    public SendMessage welcomeMessage(Update update) {
        SendMessage message;
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        UserDTO user = resourceManagerService.findUserByTelegramId(update.getMessage().getFrom().getId());

        if(user != null){
            Action actionInProgress = resourceManagerService.getActionInProgress(user.getTelegramUserId());
            if(actionInProgress != null){
                return printParametersSelection(actionInProgress,update.getMessage().getChatId());
            }
        }

        message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(String.format("%s,\nScegli tra le seguenti opzioni:", user == null ? "Benvenuto nel sistema Leucam Print Manager" : "Ciao " + user.getName()));

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        if (user == null) {
            rowInline1.add(new InlineKeyboardButton().setText("Iscrizione").setCallbackData("iscrizione"));
        } else {
            rowInline1.add(new InlineKeyboardButton().setText("Stampa Immediata").setCallbackData("stampaImmediata"));
            rowInline2.add(new InlineKeyboardButton().setText("Cancellazione").setCallbackData("cancellazione"));
            rowInline3.add(new InlineKeyboardButton().setText("Invia avviso agli iscritti").setCallbackData("advertising"));
        }

        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        if (user != null && user.getAdministrator()) {
            rowsInline.add(rowInline3);
        }

        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    @Override
    public SendMessage message(Long chat_id, String text) {
        return new SendMessage()
                .setChatId(chat_id)
                .setText(text);
    }

    @Override
    public SendMessage printParametersBackFont(Action actionInProgress, Long chat_id) {
        SendMessage message;
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        message = new SendMessage()
                .setChatId(chat_id)
                .setText(actionInProgress.toString()+"\n\nScegli se preferisci una stampa fronte o fronte/retro:");

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Fronte").setCallbackData("printParametersBackFont#" + FrontBackType.FRONT));
        rowInline1.add(new InlineKeyboardButton().setText("Fronte/Retro").setCallbackData("printParametersBackFont#" + FrontBackType.FRONT_BACK));
        rowsInline.add(rowInline1);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    @Override
    public SendMessage printParametersCopies(Action actionInProgress, Long chat_id) {
        SendMessage message;
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        message = new SendMessage()
                .setChatId(chat_id)
                .setText(actionInProgress.toString()+"\n\nIndicare la quantità da stampare scrivendo soltanto il numero");
        return message;
    }

    @Override
    public SendMessage printParametersPagesPerSheet(Action actionInProgress, Long chat_id) {
        SendMessage message;
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        message = new SendMessage()
                .setChatId(chat_id)
                .setText(actionInProgress.toString()+"\n\nScegli quante pagine per foglio stampare:");

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("1 pagina per foglio").setCallbackData("printParametersPagesPerSheet#1"));
        rowInline2.add(new InlineKeyboardButton().setText("2 pagina per foglio").setCallbackData("printParametersPagesPerSheet#2"));
        rowInline3.add(new InlineKeyboardButton().setText("4 pagina per foglio").setCallbackData("printParametersPagesPerSheet#4"));
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    @Override
    public SendMessage printParametersGrayScaleOrColor(Action actionInProgress, Long chat_id) {
        SendMessage message;
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        message = new SendMessage()
                .setChatId(chat_id)
                .setText(actionInProgress.toString()+"\n\nScegli se preferisci una stampa in bianco e nero o a colori:");

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Bianco e nero").setCallbackData("printParametersGrayScaleOrColor#" + ColorType.GRAY_SCALE));
        rowInline1.add(new InlineKeyboardButton().setText("Colore").setCallbackData("printParametersGrayScaleOrColor#" + ColorType.COLOR));
        rowsInline.add(rowInline1);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    @Override
    public SendMessage printParametersSelection(Action actionInProgress, Long chat_id){
        SendMessage message = null;
        if(actionInProgress.getColorType() == null){
            message = printParametersGrayScaleOrColor(actionInProgress,chat_id);
        } else if(actionInProgress.getFrontBackType() == null){
            message = printParametersBackFont(actionInProgress,chat_id);
        } else if(actionInProgress.getPagesPerSheet() == null){
            message = printParametersPagesPerSheet(actionInProgress,chat_id);
        } else if(actionInProgress.getNumberOfCopies() == null){
            message = printParametersCopies(actionInProgress,chat_id);
        }

        return message;
    }
}
