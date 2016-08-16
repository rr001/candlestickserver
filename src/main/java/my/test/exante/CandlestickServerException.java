package my.test.exante;

public class CandlestickServerException extends Exception {
    public CandlestickServerException(String message) {
        super(message);
    }

    public CandlestickServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
