package uz.pdp;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.bot.TarvuzMarketBot;
import uz.pdp.service.CategoryService;



public class BotMain {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TarvuzMarketBot());  // bu RentoBot class’ini chaqiryapti
            System.out.println("✅ RentoBot started successfully!");
        } catch (TelegramApiException e) {
            System.out.println("❌ Bot failed to start: " + e.getMessage());
        }
    }
}
