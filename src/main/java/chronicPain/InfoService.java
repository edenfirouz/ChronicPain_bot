package chronicPain;

import chronicPain.authorization.Picard.ResponseListener;
import chronicPain.data.Global;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * This class is the information of the patient.
 */
public class InfoService {
    private static final String TAG = "chronicPain.InfoService";
    private static String ACTIVATE_SERVICE_PARAMETER_NAME = "patientInitCode";
    public static String ACTIVATE_SERVICE_URL = Global.SERVICE_ROOT_URL + "ActivatePatient";


    /**
     * This function send http request to the server.
     * @param patientCode - The code that the user inserted
     * @param responseListener - The listener
     * @param chatID -The chat ID of the user
     */
    public static void activateUser(final String patientCode , final ResponseListener responseListener,Long chatID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResponse response;
                    HttpClient client = HttpClientBuilder.create().build();
                    JSONObject json = new JSONObject();

                    HttpPost postRequest = new HttpPost(ACTIVATE_SERVICE_URL);
                    postRequest.setHeader("Content-type", "application/json; charset=utf-8");
                    postRequest.setHeader("Accept", "text/javascript");

                    json.put(ACTIVATE_SERVICE_PARAMETER_NAME, patientCode);

                    ByteArrayEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF8"));
                    entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));

                    postRequest.setEntity(entity);
                    response = client.execute(postRequest);

                    //this function chck if the code that the user inserted is correct.
                    responseListener.activateUserResponse(response,patientCode,chatID);

                } catch (ClientProtocolException e) {
                } catch (IOException e) {
                } catch (JSONException e) {
                }
            }
        }).start();

    }
}