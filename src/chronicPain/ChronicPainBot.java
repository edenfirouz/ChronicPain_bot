package chronicPain;

import chronicPain.authorization.Picard.Picard;
import chronicPain.authorization.Picard.ResponseListener;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 *the  class connect to thelegram service and handle response of Picard and user
 */
public class ChronicPainBot extends TelegramLongPollingBot implements ResponseListener {

    //    private boolean newUser = true;
    // private JSONObject jsonOfPicard;
    private final Map<Long, Boolean> newUser = new HashMap<>();
    private final Map<Long, String[]> users = new HashMap<Long, String[]>();
    private final Map<Long, JSONObject> jsonUsers = new HashMap<>();

    /**
     * Responsible to manage what to do when user send message to the bot
     * check validation of the message
     * @param update
     */
    public void onUpdateReceived(final Update update) {
        String ansFromUser = update.getMessage().getText();
        Long chatID = update.getMessage().getChatId();
//                if (newUser && users.get(chatID) == null) {
        /**
         * when the it's new user
         */
        if (newUser.get(chatID) == null) {
            if (ansFromUser.contains("שלום")) {
                newUser.put(chatID, false);
//                        users.put(chatID, new String[3]);
                sendTextMessage("_"+update.getMessage().getText()+"_", "ברוך הבא לטל-כאב!\nאנא הזן את הקוד שקיבלת מהרופא", chatID);
//                        newUser = false;
            } else {
                sendTextMessage(ansFromUser+"_לא שלום_", "אנא הכנס את מילת המפתח", chatID);
            }
        } else {
            if (users.get(chatID) == null && ansFromUser.length() == 4) {//code
                InfoService.activateUser(update.getMessage().getText(), this, chatID);
            } else if (users.get(chatID) == null) {
                sendTextMessage("_קוד לא תקין_" + ansFromUser, "אנא הכנס קוד תקין", chatID);
//            } else if ((users.get(chatID)[1] != null && !didAnHourPass(chatID) && users.get(chatID)[2] == "false")||users.get(chatID)[2] == "true") {
             } else if (users.get(chatID)[2] == "true") {
                sendTextMessage("_" + ansFromUser + "_", "אינך יכול לדווח כרגע. נא המתן עד תום השעה.", chatID);
                /**
                 * handle with answers of users for Picard question
                 */
            } else if (ansFromUser.length() == 1 && isNumeric(ansFromUser)) {
                markTheAns(Integer.parseInt(ansFromUser), chatID);
                int ansUser = Integer.parseInt((ansFromUser));
                if (((JSONObject) ((JSONArray) getJSON(chatID).get("Messages")).get(0)).get("ConceptName").toString().contains("דקות")) {
                    if (ansUser < 1 || ansUser > 4) {
                        sendTextMessage(ansFromUser + " _wrong input_", "אנא הכנס תשובה תקינה", chatID);
                        return;
                    } else {
                        String whatIs = ((JSONObject) ((JSONArray) getJSON(chatID).get("Messages")).get(Integer.parseInt(ansFromUser) - 1)).get("ConceptName").toString();
                        System.out.println(new java.util.Date() + " not pass hour");

                        if (whatIs.contains("20 דקות")) {
                            putIntoUser(chatID, "true", 2);
                            doSnoozeOnce(40, chatID);
                            System.out.println(new java.util.Date() + " not pass hour");
                        } else if (whatIs.contains("40 דקות")) {
                            putIntoUser(chatID, "true", 2);
                            doSnoozeOnce(20, chatID);
                            System.out.println(new java.util.Date() + " not pass hour");
                        } else if (whatIs.contains("0-30 דקות")) {
                            putIntoUser(chatID, "true", 2);
                            doSnoozeOnce(60, chatID);
                            System.out.println(new java.util.Date() + " not pass hour");
                        } else if (whatIs.contains("30-60 דקות")) {
                            putIntoUser(chatID, "true", 2);
                            doSnoozeOnce(30, chatID);
                            System.out.println(new java.util.Date() + " not pass hour");
                        }
                    }
                } else if (((JSONObject) ((JSONArray) getJSON(chatID).get("Messages")).get(0)).get("ConceptName").toString().contains("רמת הכאב")) {
                    if (ansUser < 0 || ansUser > 10) {
                        sendTextMessage(ansFromUser + "_wrong input_", "אנא הכנס תשובה תקינה", chatID);
                        return;
                    }
//                    else if (ansUser >= 0 && ansUser < 3){}
                    else {
                        long lastEnter = System.currentTimeMillis();
                        putIntoUser(chatID, lastEnter + "", 1);
                    }
                } else if (getJSON(chatID).get("CurrentResponseTitle").equals("מדוע לא נטלת את התרופה?")) {
                    if (ansUser < 1 || ansUser > 5) {
                        sendTextMessage(ansFromUser + "_wrong input_", "אנא הכנס תשובה תקינה", chatID);
                        return;
                    }
                }

                Picard.sendToPicardResponse(Picard.GET_RECOMMENDATIONS, getJSON(chatID), this, chatID);
//                if (pain) {
//                    System.out.println(new java.util.Date() + " pain big than 5");
//                    doSnoozeOnce(60, chatID);
//                }
            } else if(ansFromUser.contains("לדווח")){
                String[] details = users.get(chatID);
                String codeId = details[0];
                codeIsOk(codeId, chatID);
            }else {
                sendTextMessage(ansFromUser+"_wrong input_", "אנא הכנס תשובה תקינה", chatID);
            }
        }
    }

    /**
     * help function
     * @param chatID - ID for which user
     * @return the last JSON of the user according to the chatID of hashMap jsonUsers
     */
    private JSONObject getJSON(long chatID) {
        return jsonUsers.get(chatID);
    }

    /**
     * help function - responsible to activate timer to snoozing while X time document if a timer of snoozing activated for specific user
     * @param time - X time to activate the timer
     * @param chatID - ID for which user
     */
    private void doSnoozeOnce(int time, final Long chatID) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String[] details = users.get(chatID);
                String codeId = details[0];
                if (details[2] == "true") {
                    details[2] = "false";
                    users.put(chatID, details);
                }
                codeIsOk(codeId, chatID);
            }
        }, time * 60 * 1000);
    }

    /**
     * responsible to put the answer of the user in the JSON to Picard
     * @param ans - the answer of the user
     * @param chatID - ID for which user
     * @return true/false if the answer to the pain level is over 4
     */
    private void markTheAns(int ans, long chatID) {
//        boolean pain = false;
        JSONArray messages = getJSON(chatID).getJSONArray("Messages");
        if (messages.length() != 1) {
            ans = ans - 1;
            for (int i = 0; i < messages.length(); i++) {
                try {
                    JSONObject jsonObject = (JSONObject) messages.get(i);
                    if (i == ans)
                        jsonObject.put("ResultContent", "TRUE");
                    else
                        jsonObject.put("ResultContent", "FALSE");
                    messages.put(i, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            /**
             * handle the answer of the pain level question
             * to insert it to the JSON
             */
        } else {
            if (ans > 2)
                putIntoUser(chatID, "true", 2);
            System.out.println("pain level: "+ans);
            JSONObject jsonObject = (JSONObject) messages.get(0);
            jsonObject.put("ResultContent", ans);
        }
        getJSON(chatID).put("Messages", messages);
    }

    /**
     * help fanction - check if string is numeric number
     * @param str - the string it check
     * @return - true/false
     */
    private static boolean isNumeric(String str) {
        return str.matches("\\d");  //match a number with optional '-' and decimal.
    }

    /**
     * @return the username of the Bot
     */
    public String getBotUsername() {
        return "Chronic_Pain_bot";
    }

    /**
     * @return the token of the Bot
     */
    public String getBotToken() {
        return "712373400:AAFvEfolHFXPRfhgl_Kc6nWdUZuOF17kMsU";
    }

    /**
     * help function - send message to user according to the chatID
     * @param message - note for the logs
     * @param textNewMessage - message for the user
     * @param chatID - ID for which user
     */
    private void sendTextMessage(String message, String textNewMessage, long chatID) {
        //print the message
        System.out.println("Message: " + message);
        SendMessage newMessage = new SendMessage();
        newMessage.setText(textNewMessage);
        newMessage.setChatId(chatID);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * help function - send image to user according to the chatID
     * @param message - note for the logs
     * @param textNewMessage - image for the user
     * @param chatID - ID for which user
     */
    private void sendImage(String message, String textNewMessage, long chatID) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatID);
        // for run locally
//        sendPhotoRequest.setPhoto(new File("resources\\PainLevel.png"));
        // for JAR
        sendPhotoRequest.setPhoto(new File("PainLevel.png"));
        try {
            execute(sendPhotoRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * build the keyboard of the pain level
     * @param chatID - ID for which user
     */
    private void sendOptionMessage( long chatID) {

        SendMessage newMessage = new SendMessage();
        ReplyKeyboard markup = new ReplyKeyboardMarkup();


        final List<KeyboardRow> keyboard = ((ReplyKeyboardMarkup) markup).getKeyboard();
        for (int i = 0; i < 11; i++) {
            if (keyboard.isEmpty() || (keyboard.get(keyboard.size() - 1).size() >= 6)) {
                keyboard.add(new KeyboardRow());
            }
            KeyboardButton keyboardButtonI = new KeyboardButton();

            keyboardButtonI.setText("    " + i + "    ");
            keyboard.get(keyboard.size() - 1).add(keyboardButtonI);
        }
        newMessage.setReplyMarkup(markup);
        newMessage.setText("מהי רמת הכאב שלך?");
        newMessage.setChatId(chatID);

        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * this function check the code of the patient
     * @param response - the Httpresponse of the user
     * @param patientCode - ID of the user according to Picard
     * @param chatID - ID for which user
     */
    public void activateUserResponse(HttpResponse response, String patientCode, Long chatID) {
        if (response != null && !response.equals("")) {
            String responseAnswer = null;
            try {
                responseAnswer = EntityUtils.toString(response.getEntity());
                responseAnswer = responseAnswer.replaceAll("\"", "");
                if (!responseAnswer.contains("Exception") && !responseAnswer.contains("There is no row at position 0"))
                    codeIsOk(responseAnswer, chatID);
                else codeIsBad(chatID, patientCode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else codeIsBad(chatID,patientCode);
    }

    /**
     * if the code the user insert not good ask the user to fix the code
     * @param chatID - ID for which user
     * @param code
     */
    private void codeIsBad(long chatID,String code) {
        sendTextMessage(code+"_CodeIsBad_", "אנא הכנס קוד תקין", chatID);
    }

    /**
     * send the next question to user according to the JSON of Picard
     * @param stringResponse - the JSON of picard  (String)
     * @param chatID - ID for which user
     */
    public void handleResponse( final String stringResponse, Long chatID) {
        JSONObject response = new JSONObject(JsonService.clearOldValue(stringResponse));
        jsonUsers.put(chatID, response);
        JSONArray messages = (JSONArray) response.get("Messages");
        if (((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("20277")|| ((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("17828") || ((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("20149") ||((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("17798") ) {
            doSnoozeOnce(60, chatID);
            putIntoUser(chatID, "true", 2);

        }
        if (response.get("CurrentResponseTitle").equals("מתי נטלת את התרופה ?")) {
            if (((JSONObject) messages.get(0)).get("ConceptName").equals("20 דקות"))
                sendTextMessage("מתי נטלת 20_40", "מתי נטלת את התרופה?\n" +
                        "1. לפני 20 דקות.\n" +
                        "2. לפני 40 דקות.\n" +
                        "3. לפני שעה או יותר.\n" +
                        "4. לא נטלתי.\n" +
                        "רשום את מספר הבחירה (1-4).\n", chatID);

            else if (((JSONObject) messages.get(0)).get("ConceptName").equals("0-30 דקות"))
                sendTextMessage("מתי נטלת 30_60", "מתי נטלת את התרופה?\n" +
                        "1. 0-30 דקות.\n" +
                        "2. 30-60 דקות.\n" +
                        "3. לפני שעה או יותר.\n" +
                        "4. לא נטלתי.\n" +
                        "רשום את מספר הבחירה (1-4).\n", chatID);
        } else if ((((JSONObject) messages.get(0)).get("__type").equals("JsonService:#SpockAppStructs.Messages")) || (((JSONObject) messages.get(0)).get("__type").equals("Recommendation:#SpockAppStructs.Messages"))) {
            String recommendation = ((JSONObject) messages.get(0)).get("MessageName").toString();
            sendTextMessage(JsonService.buildRecommendation(recommendation)+" _המלצה_ ", JsonService.buildRecommendation(recommendation), chatID);

//        } else if (((JSONObject) messages.get(0)).get("ConceptName").equals("אם רמת הכאב שבה אתה חש שווה לאפס ?")) {
        } else if (((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("17796") || ((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("17892") || ((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("17783")|| ((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("20271")|| ((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("20425")|| ((JSONObject) messages.get(0)).get("GuidelineID").toString().equals("17781")) {
            sendOptionMessage(chatID);
            sendImage("רמת כאב", "", chatID);
        } else if ((((JSONObject) messages.get(0)).get("ConceptName").equals("מהי רמת הכאב?"))) {
            sendOptionMessage(chatID);
            sendImage("רמת כאב", "", chatID);
        } else if (getJSON(chatID).get("CurrentResponseTitle").equals("מדוע לא נטלת את התרופה?")) {
            sendTextMessage("מדוע לא נטלת", "מדוע לא נטלת את התרופה?\n" +
                    "1. שכחתי.\n" +
                    "2. פחדתי מתופעות לוואי.\n" +
                    "3. לא מסוגל לבלוע.\n" +
                    "4. כאב חלף מעצמו.\n" +
                    "5. תרופה לא זמינה.\n" +
                    "רשום את מספר הבחירה (1-5).\n", chatID);
        }
    }

    /**
     * if the code is ok keep the detail of the user in hashMap users
     * @param id - ID of user according to Picard
     * @param chatID - ID for which user
     */
    public void codeIsOk(String id, Long chatID) {
        String[] details = new String[3];
        if (users.get(chatID) == null) {
            details[0] = id;
            details[2] = "false";
            users.put(chatID, details);
        }
        weHaveId(id, chatID);
    }

    /**
     *  help function that update details in the array of the user in the HashMap
     * @param chatID -ID for which user
     * @param enter - the detail to insert to the value of the array
     * @param index - in which index in the array
     */
    private void putIntoUser(long chatID, String enter, int index) {
        String[] details = users.get(chatID);
        details[index] = enter;
        users.put(chatID, details);
    }

    /**
     * help function that after we have all the details we send to Picard request to start session and than handle with the response
     * @param id - ID of user according to Picard
     * @param chatID - ID for which user
     */
    private void weHaveId(String id, Long chatID) {
        String ansToString = Picard.startNewSession(id);
        try {
            handleResponse(ansToString, chatID);

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * check if past one hour from the last reporting of level pain
     * @param chatID - ID for which user
     * @return true/false
     */
    private boolean didAnHourPass(long chatID) {

        long lastEnter = Long.valueOf(users.get(chatID)[1]).longValue();
        return lastEnter + 60 * 1000 * 60 // waiting time
                < System.currentTimeMillis();
    }

    /**
     *  responsible to activate the snoozing to user in 3 randoms time  for all the user in the hashMap users
     */
    public void snoozing() {

        if (users.size()>0 && ( shouldSetAlarm(7) || shouldSetAlarm(13) || shouldSetAlarm(19))) {
            System.out.println(new java.util.Date() + "snoozing 3 times");
            int minNow = GregorianCalendar.getInstance().get(GregorianCalendar.MINUTE);
            for (Map.Entry<Long, String[]> entry : users.entrySet()) {
                Long user = entry.getKey();
                if (users.get(user)[2] == "false") {
                    int waitTime = (int) (Math.random() * (119 - minNow));
                    System.out.println(waitTime + "to wait");
                    doSnoozeOnce(waitTime, user);
                   // doSnoozeOnce(0, user);
                } else
                    System.out.println("another timer will work soon");
            }
        }
    }

    /**
     * help function check if now is the time to snooze
     * @param hourAlarm
     * @return true/false
     */
    private static boolean shouldSetAlarm(int hourAlarm) {
        GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        int hourNow = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY);
        boolean ans = hourNow == hourAlarm;
        System.out.println(ans + ":" + hourAlarm + " but hour now is " + hourNow);
        return ans;
    }
}












