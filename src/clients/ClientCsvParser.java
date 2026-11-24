package clients;

public class ClientCsvParser {
    public ClientDraft parse(String line, int lineNumber) throws ImportRowException {
        if (line == null || line.isBlank()) {
            throw new ImportRowException("Пустая строка");
        }
        String[] parts = line.split(String.valueOf(ValidationRules.DELIMITER), -1);
        if (parts.length != ValidationRules.FIELD_COUNT) {
            throw new ImportRowException("Ожидалось " + ValidationRules.FIELD_COUNT
                    + " поля, получено " + parts.length);
        }
        return new ClientDraft(lineNumber, parts[0], parts[1], parts[2], parts[3]);
    }
}
