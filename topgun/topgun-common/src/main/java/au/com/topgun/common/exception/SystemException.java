package au.com.topgun.common.exception;

public class SystemException extends Exception {

    public SystemException(String message) {
        super(message);
    }
    
    public SystemException(String message, Throwable cause) {
        super (message, cause);
    }
    
    public SystemException(Throwable cause) {
        super(cause);
    }
}
