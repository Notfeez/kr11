package clients;

public final class ValidationRules {
    public static final int INN_SHORT_LENGTH = 10;
    public static final int INN_LONG_LENGTH = 12;
    public static final int MAX_DUPLICATE_REPLACEMENTS = 35;
    public static final int FIELD_COUNT = 4;
    public static final char DELIMITER = ';';

    private ValidationRules() {
    }

    public static boolean canReplaceDuplicate(int replacedSoFar) {
        return replacedSoFar < MAX_DUPLICATE_REPLACEMENTS;
    }
}
