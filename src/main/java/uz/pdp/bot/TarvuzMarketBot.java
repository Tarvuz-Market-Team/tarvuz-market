package uz.pdp.bot;

import com.sun.xml.internal.ws.resources.SenderMessages;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class TarvuzMarketBot extends TelegramLongPollingBot {
    private static final String BOT_NAME = "t.me/tarvuz_market_bot";
    private static final String BOT_TOKEN = "7424451290:AAFDeTaHv1yZiRgqwG3A3uEuJNErNwcZCk0";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            User user = update.getMessage().getFrom();

            SendMessage sendMessage = getSendMessage(chatId, text, user);

            try {
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static SendMessage getSendMessage(long chatId, String text, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (text.equals("/start")) {
            sendMessage.setText("Welcome to Tarvuz Market Bot, " + user.getFirstName() + "!");
        } else if (text.equals("/help")) {
            sendMessage.setText("Here are the commands you can use:\n" +
                    "/start - Start the bot\n" +
                    "/help - Show this help message");
        } else {
            sendMessage.setText("You said: " + text);
        }
        return sendMessage;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }
}
