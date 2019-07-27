package chronicPain.data;

/**
 * This class contains the properties of the server.
 */
public class Global {

    //SERVICE

    public  static String SERVICE_ROOT_URL = "https://medinfo2.ise.bgu.ac.il/ChronicPainTest/WCF/Service1.svc/jsonp/";
    public  static String PICARD_URL = "https://medinfo2.ise.bgu.ac.il/chronicPain.authorization.Picard/PicardWCFServicesTest/PicardWCFServer.ClientEMRService.svc/jsonp/";
    //public  static String SERVICE_ROOT_URL = "https://medinfo2.ise.bgu.ac.il/ChronicPainTest/WCF/Service1.svc/jsonp/";
    //public  static String PICARD_URL = "https://medinfo2.ise.bgu.ac.il/Picard/PicardWCFServicesTest/PicardWCFServer.ClientEMRService.svc/jsonp/";

    /**
     * This function set the url to the test server.
     */
    public  static void setURL() {
        //SERVICE
        SERVICE_ROOT_URL = "https://medinfo2.ise.bgu.ac.il/ChronicPainTest/WCF/Service1.svc/jsonp/";
        PICARD_URL = "https://medinfo2.ise.bgu.ac.il/chronicPain.authorization.Picard/PicardWCFServicesTest/PicardWCFServer.ClientEMRService.svc/jsonp/";
    }

    /**
     * @return the service url
     */
    public static String getRootURL(){
        return SERVICE_ROOT_URL;
    }

    /**
     * @return the Picard url
     */
    public static String getPicardURL(){
        return PICARD_URL;
    }
}