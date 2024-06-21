package ru.lbarbaris.telegrambot.service;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lbarbaris.telegrambot.config.BotConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Component

public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    final  BotConfig botConfig;
    List<BotCommand> botCommandList;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "welcome message"));
        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e) {
            log.error("Telegram error: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

            if (update.hasMessage() &&  update.getMessage().hasText()){
                long chatId = update.getMessage().getChatId();
                String message = update.getMessage().getText();
                switch (message){
                    case "/start" -> {
                        try {
                            start(chatId, update.getMessage().getChat().getUserName());
                        } catch (TelegramApiException e) {
                            log.error("Telegram error: " + e.getMessage());
                        }
                    }
                    case "/button" -> {
                        try {
                            button(chatId);
                        }
                        catch (TelegramApiException e) {
                            log.error("Telegram error: " + e.getMessage());
                        }
                    }
                    default -> {
                        try {
                            send(chatId, "Sorry, command not found :(");
                        } catch (TelegramApiException e) {
                            log.error("Telegram error: " + e.getMessage());
                        }
                    }
                }
            }
            else if (update.hasCallbackQuery()){
                String callBackData = update.getCallbackQuery().getData();
                long idMessage = update.getCallbackQuery().getMessage().getMessageId();
                long chatId = update.getCallbackQuery().getMessage().getChatId();

                if (callBackData.equals("button1")){
                    EditMessageText messageText = new EditMessageText();
                    messageText.setChatId(String.valueOf(chatId));
                    messageText.setText("Вау! вы нажали на кнопку? как это мило!");
                    messageText.setMessageId((int) idMessage);

                    try {
                        execute(messageText);
                    }
                    catch (TelegramApiException e){
                        log.error("Telegram error: " + e.getMessage());
                    }
                }
            }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    private void start(long chatId, String name) throws TelegramApiException{
        String answer = "Привет, " + name + " " + new String(Character.toChars(0x1F920));
        send(chatId, answer);
    }


    private void button(long chatId) throws TelegramApiException{
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> inLine = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Проверяем текст у кнопки!");
        button1.setCallbackData("button1");

        inLine.add(button1);
        rows.add(inLine);
        inlineKeyboardMarkup.setKeyboard(rows);


        message.setReplyMarkup(inlineKeyboardMarkup);
        String answer = "Тестируем кнопку у сообщения! " + new String(Character.toChars(0x1F62F));
        message.setText(answer);
        try {
            execute(message);
        }
        catch (TelegramApiException e){
            log.error("Telegram error: " + e.getMessage());
        }
    }

    private void send(long chatId, String text) throws TelegramApiException{
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("/button");
        row.add("test2");
        row.add("test3");
        keyboardRows.add(row);
        KeyboardRow row2 = new KeyboardRow();
        row2.add("test4");
        keyboardRows.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(replyKeyboardMarkup);


        try {
            execute(message);
        }
        catch (TelegramApiException e){
            log.error("Telegram error: " + e.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
}
