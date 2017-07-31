import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {

    public static void main(String[] args) {
        //init api
        ApiContextInitializer.init();
        //inst bots api
        TelegramBotsApi botsApi = new TelegramBotsApi();
        //register bot
        try {
            //name = @MyTest248Bot
            botsApi.registerBot(new MyTestBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println("Bot successfully started");
    }

}
