package chronicPain.authorization.Picard;

import org.apache.http.HttpResponse;

/**
 * This interface is listener that response to Http request.
 */
public interface ResponseListener {
    /*
     * @param response - response to Http request.
     * @param patientCode - the code of the patient
     * @param chatID - the chat ID of the patient
     */
    public void activateUserResponse(HttpResponse response, String patientCode,Long chatID);
    public void handleResponse(String response ,Long chatID);
}