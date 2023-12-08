package org.example.services;
import org.example.controllers.Entry;
import org.example.resources.EnvironmentVariables;
import org.example.resources.TextMessages;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Random;

import static org.example.services.Database.logTracker;

public class Bot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return EnvironmentVariables.BOT_NAME;
    }
    @Override
    public String getBotToken() {
        return EnvironmentVariables.BOT_TOKEN;
    }
    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        String command = msg.getText();
        String name = msg.getFrom().getFirstName();
        if(msg.hasText()){
            switch (command){
                case "/start":
                    startCase(update);
                    break;
                case "/log":
                    logCase(update);
                    break;
                case "/endlog":
                    endLogCase(update);
                    break;
                case "/prompt":
                    promptCase(update);
                    break;
                case "/curate":
                    System.out.println(name + " wants to curate");
                    break;
                case "/help":
                    helpCase(update);
                    break;
                default:
                    if(!sessionCheck(update)){
                        defaultCase(update);
                    } else {
                        logFunction(update);
                    }
                    break;
            }
        } else {
            if(!sessionCheck(update)){
                defaultCase(update);
            } else {
                logFunction(update);
            }
        }
    }
    public void messageSender(Update update, String message){
        SendMessage msgResponse =  new SendMessage();
        msgResponse.setChatId(update.getMessage().getChatId().toString());
        msgResponse.setText(message);
        try{
            execute(msgResponse);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    public void insertIntoDB(Update update){
        Entry entry = convertUpdateToEntry(update);
        Database.updateUserEntry(entry);
    }
    public Entry convertUpdateToEntry(Update update){
        Message msg = update.getMessage();
        String name = update.getMessage().getFrom().getFirstName();
        Long id = update.getMessage().getFrom().getId();

        return new Entry(name, id, msg);
    };
    public boolean sessionCheck(Update update){
        String key = String.valueOf(update.getMessage().getFrom().getId());
        return logTracker.get(key);
    }
    public void startCase(Update update){
        String name = update.getMessage().getFrom().getFirstName();
        messageSender(update, TextMessages.WELCOME_TEXT +name+"!");
        wait(1500);
        messageSender(update, TextMessages.INTRO_TEXT);
        wait(7000);
        messageSender(update, TextMessages.CONTEXT);
        wait(2500);
        messageSender(update, TextMessages.EXPLAINER);
        wait(6000);
        messageSender(update, TextMessages.WELL_WISHES);
        insertIntoDB(update);
    }
    public void logCase(Update update){
        if(sessionCheck(update)){
            messageSender(update, TextMessages.LOG_ALREADY_ACTIVE);
        } else{
            System.out.println(update.getMessage().getFrom().getFirstName() + " is  logging");
            messageSender(update, TextMessages.LOG_MSG);
            String key = String.valueOf(update.getMessage().getFrom().getId());
            logTracker.put(key, true);
        }

    }
    public void logFunction(Update update){
        Entry entry = convertUpdateToEntry(update);
        Database.addToLogArray(entry);
    }
    public void endLogCase(Update update){
        //bundle logs
        if(sessionCheck(update)){
            System.out.println(update.getMessage().getFrom().getFirstName() + " is ending logging");
            Entry entry = convertUpdateToEntry(update);
            Database.updateUserLogs(entry);
            messageSender(update, TextMessages.END_LOG_MSG);
            String key = String.valueOf(update.getMessage().getFrom().getId());
            logTracker.put(key, false);
            Database.messagelogs.clear();
        } else{
            messageSender(update, TextMessages.LOG_ALREADY_OVER);
        }

    }
    public void promptCase(Update update){
        Random rand = new Random();
        int num = rand.nextInt(3);
        switch (num){
            case 0:
                messageSender(update, TextMessages.PROMPT1);
                break;
            case 1:
                messageSender(update, TextMessages.PROMPT2);
                break;
            case 2:
                messageSender(update, TextMessages.PROMPT3);
                break;
            default:
                messageSender(update, TextMessages.PROMPT_PLACEHOLDER);
                break;
        }
    }
    public void helpCase(Update update){
        messageSender(update, TextMessages.EXPLAINER);
    }
    public void defaultCase(Update update){
        insertIntoDB(update);
        messageSender(update, TextMessages.ERROR_MSG );
    }
    public static void wait(int ms) {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
}
