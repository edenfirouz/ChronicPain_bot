package chronicPain;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class MainClass {
    /**
     * create connection to telegram an initial it.
     * @param args
     */
    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            final ChronicPainBot chronicPainBot=new ChronicPainBot();
            telegramBotsApi.registerBot(chronicPainBot);
            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
            /**
             * create the timer to run every hour and check the time
             */
            ses.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                        chronicPainBot.snoozing();
                }
            }, 0, 1, TimeUnit.HOURS);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
