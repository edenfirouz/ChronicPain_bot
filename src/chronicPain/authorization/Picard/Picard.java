package chronicPain.authorization.Picard;

import chronicPain.authorization.ChronicException;
import chronicPain.data.Global;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * the class responsible to the connection with Picard and send request for it
 */
public class Picard {
    private static final String START_SESSION = "StartNewSessionForJson";
    public static final String GET_RECOMMENDATIONS = "GetRecommendationsForJson";
    private static final String TAG = "chronicPain/authorization/Picard";


    /**
     * start new session with Picard , includes the details of the user
     * @param id - ID of user according to Picard
     * @return the final response to the request from picard
     */
    public static String startNewSession(String id) {
        try {
            JSONObject json = new JSONObject();
            String patientID=id;
            json.put("patientID", patientID);
            json.put("patientName", "xxx");
            json.put("glTitle", "Chronic Pain");
            json.put("glDocID", "17777");
            json.put("clinicName", "ICU");
            json.put("ClinicalSettingsType", 4);
            return sendToPicardTime(START_SESSION,json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * insert the current time to the request to Picard
     * @param url - of Picard
     * @param jsonObject
     * @return - the response of Picard
     */
    public static String sendToPicardTime(String url, JSONObject jsonObject) {
        TimeZone timeZone = TimeZone.getDefault();
        Date date = Calendar.getInstance(timeZone).getTime();
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        long adder = 60 * 1000 * 60*3;
        adder += calendar.getTimeInMillis();
        try {
            jsonObject.put("startTime", "/Date(" + adder + ")/");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendToPicardRequest(url, jsonObject.toString());
    }

    /**
     * insert to the request more necessary details
     * @param url  - of Picard
     * @param jsonObject - the request of the user
     * @param responseListener -This interface is listener that response to http request
     * @param chatID - ID for which user
     */
    public static void sendToPicardResponse(final String url, final JSONObject jsonObject,final ResponseListener responseListener, long chatID) {
        checkCargo(url, jsonObject);
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject ans = new JSONObject();
                try {
                    ans.put("sessionDetails", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String response = sendToPicardTime(url, ans);
                try {
                    if (!response.equals("")) {
                        if (chatID!=-1)
                            responseListener.handleResponse(response,chatID);
                    }
                } catch (JSONException e) {
                }

            }
        }).start();
    }

    /**
     * check exceptions
     * @param url  - of Picard
     * @param jsonObject - the request of the user
     */

    public static void checkCargo(final String url, final JSONObject jsonObject) {
        if (url == null || url.equals(""))
            throw new ChronicException("CheckCargo", "url is empty or null");
        if (jsonObject == null)
            throw new ChronicException("CheckCargo", "jsonObject is null");
    }

    /**
     * finally send the request to Picard and get resonse from it
     * @param url  - of Picard
     * @param json - the request of the user
     * @return - response of Picard
     */
    public static String sendToPicardRequest(String url, String json) {
        try {
            json=clearOldValue(json);
            HttpResponse httpResponse;
            HttpClient client = HttpClientBuilder.create().build();
            String tempUri=Global.PICARD_URL + url;
            String newUri=tempUri.replace("chronicPain.authorization.Picard","Picard");
            HttpPost request = new HttpPost(newUri);// URL
            request.setHeader("Content-type", "application/json; charset=utf-8");
            request.setHeader("Accept", "text/javascript");
            StringEntity se = new StringEntity(json);
            request.setEntity(se);
            httpResponse = client.execute(request);
            String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            return response;

        } catch (UnsupportedEncodingException e) {
            throw new ChronicException(e.getMessage(), "UnsupportedEncodingException");
        } catch (ClientProtocolException e) {
            throw new ChronicException(e.getMessage(), "ClientProtocolException");
        } catch (IOException e) {
            throw new ChronicException("אנא וודא כי הינך מחובר לרשת ה-Wifi או הרשת הסלולרית", "IOException1");
        } catch (IllegalArgumentException e) {
            throw new ChronicException(e.getMessage(), "IllegalArgumentException");
        }
    }

    /**
     * @param json - string of json object with "OldValue".
     * @return - string of json object without "OldValue".
     */
    private static String clearOldValue(String json) {
            json=json.replaceAll("\"OldValue\":\"FALSE\",","");
            json=json.replaceAll("\"OldValue\":\"TRUE\",","");
        return json;
    }




}
