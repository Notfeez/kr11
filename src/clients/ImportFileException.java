package clients;

public class ImportFileException extends Exception {
    public ImportFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportFileException(String message) {
        super(message);
    }
}
