package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientImporter {
    private final ClientCsvParser parser;
    private final ClientValidator validator;
    private final ClientRepository repository;

    public ClientImporter(ClientCsvParser parser, ClientValidator validator, ClientRepository repository) {
        this.parser = parser;
        this.validator = validator;
        this.repository = repository;
    }

    public ImportResult importFile(Path path) throws ImportFileException {
        if (path == null) {
            throw new ImportFileException("Путь к файлу не указан");
        }
        ImportResult result = new ImportResult();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new ImportFileException("Файл пуст: нет строки заголовка");
            }
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                processLine(line, lineNumber, result);
            }
        } catch (NoSuchFileException e) {
            throw new ImportFileException("Файл не найден: " + path, e);
        } catch (IOException e) {
            throw new ImportFileException("Не удалось прочитать файл " + path + ": " + e, e);
        }
        return result;
    }

    private void processLine(String line, int lineNumber, ImportResult result) {
        ClientDraft draft;
        try {
            draft = parser.parse(line, lineNumber);
        } catch (ImportRowException e) {
            result.addError(lineNumber, e.getMessage());
            return;
        }
        ValidationResult validation = validator.validate(draft);
        if (!validation.isValid()) {
            result.addError(lineNumber, validation.joinReasons());
            return;
        }
        boolean duplicate = repository.findByInn(draft.getInn()).isPresent();
        if (duplicate && !ValidationRules.canReplaceDuplicate(result.getReplaced())) {
            result.addError(lineNumber, "Дубликат ИНН " + draft.getInn() + ": лимит замен "
                    + ValidationRules.MAX_DUPLICATE_REPLACEMENTS + " за один импорт исчерпан");
            return;
        }
        repository.save(draft.toClient());
        if (duplicate) {
            result.addReplaced();
        } else {
            result.addAccepted();
        }
    }
}
