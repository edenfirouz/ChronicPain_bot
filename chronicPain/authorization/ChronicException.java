package chronicPain.authorization;

/**
 * responsible to check exception
 */
public class ChronicException extends RuntimeException {

    public String header;
    public ChronicException(String header, String message) {
        super(message);//goes to stackTrace
        this.header = header;
    }
}