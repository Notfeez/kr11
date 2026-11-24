package clients;

public class ImportError {
    private final int lineNumber;
    private final String reason;

    public ImportError(int lineNumber, String reason) {
        this.lineNumber = lineNumber;
        this.reason = reason;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "Строка " + lineNumber + ": " + reason;
    }
}
