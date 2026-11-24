package clients;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int accepted;
    private int replaced;
    private final List<ImportError> errors = new ArrayList<>();

    public void addAccepted() {
        accepted++;
    }

    public void addReplaced() {
        accepted++;
        replaced++;
    }

    public void addError(int lineNumber, String reason) {
        errors.add(new ImportError(lineNumber, reason));
    }

    public int getAccepted() {
        return accepted;
    }

    public int getReplaced() {
        return replaced;
    }

    public int getRejected() {
        return errors.size();
    }

    public List<ImportError> getErrors() {
        return List.copyOf(errors);
    }

    public String report() {
        StringBuilder sb = new StringBuilder();
        sb.append("Принято: ").append(accepted)
                .append(" (из них заменили прежнюю запись с тем же ИНН: ").append(replaced).append(")\n");
        sb.append("Отклонено: ").append(errors.size());
        for (ImportError error : errors) {
            sb.append('\n').append(error);
        }
        return sb.toString();
    }
}
