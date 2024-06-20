package ru.lbarbaris.telegrambot.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lbarbaris.telegrambot.config.BotConfig;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    final  BotConfig botConfig;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;

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
                            throw new RuntimeException(e);
                        }
                    }
                    default -> {
                        try {
                            send(chatId, "Sorry, command not found :(");
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    private void start(long chatId, String name) throws TelegramApiException{
        String answer = "Привет, " + name;
        send(chatId, answer);
    }

    private void send(long chatId, String text) throws TelegramApiException{
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        }
        catch (TelegramApiException e){

        }
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
}
