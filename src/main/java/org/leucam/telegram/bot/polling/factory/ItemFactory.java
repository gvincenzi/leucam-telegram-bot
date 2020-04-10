package org.leucam.telegram.bot.polling.factory;

import org.leucam.telegram.bot.model.Action;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ItemFactory {
    SendMessage welcomeMessage(Update update);
    SendMessage message(Long chat_id, String text);
    SendMessage credit(Long chat_id);
    SendMessage printParametersBackFont(Action actionInProgress, Long chat_id);
    SendMessage printParametersCopies(Action actionInProgress, Long chat_id);
    SendMessage printParametersPagesPerSheet(Action actionInProgress, Long chat_id);
    SendMessage printParametersGrayScaleOrColor(Action actionInProgress, Long chat_id);
    SendMessage printParametersSelection(Action actionInProgress, Long chat_id);
}
