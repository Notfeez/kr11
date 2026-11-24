package clients;

public class ClientValidator {
    public ValidationResult validate(ClientDraft draft) {
        ValidationResult result = new ValidationResult();
        checkInn(draft.getInn(), result);
        checkName(draft.getName(), result);
        checkEmail(draft.getEmail(), result);
        return result;
    }

    private void checkInn(String inn, ValidationResult result) {
        if (inn.isEmpty()) {
            result.addReason("ИНН не указан");
            return;
        }
        if (!onlyDigits(inn)) {
            result.addReason("ИНН должен состоять только из цифр");
            return;
        }
        if (inn.length() != ValidationRules.INN_SHORT_LENGTH && inn.length() != ValidationRules.INN_LONG_LENGTH) {
            result.addReason("ИНН должен содержать " + ValidationRules.INN_SHORT_LENGTH + " или "
                    + ValidationRules.INN_LONG_LENGTH + " цифр, получено " + inn.length());
        }
    }

    private void checkName(String name, ValidationResult result) {
        if (name.isEmpty()) {
            result.addReason("Имя клиента не указано");
        }
    }

    private void checkEmail(String email, ValidationResult result) {
        if (email.isEmpty()) {
            result.addReason("Email не указан");
            return;
        }
        int first = email.indexOf('@');
        int last = email.lastIndexOf('@');
        if (first < 0 || first != last) {
            result.addReason("Email должен содержать ровно один символ @");
            return;
        }
        if (first == 0 || first == email.length() - 1) {
            result.addReason("Email должен иметь непустые части слева и справа от @");
        }
    }

    private boolean onlyDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
