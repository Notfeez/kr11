package clients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SelfCheck {
    private static final String HEADER = "inn;name;email;phone";
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        typicalFile();
        innLengthBoundaries();
        innFormat();
        emailRules();
        phoneIsOptional();
        emptyFields();
        wrongFieldCount();
        duplicateReplacesPrevious();
        duplicateReplacementLimit();
        invalidDuplicateDoesNotReplace();
        importContinuesAfterError();
        severalReasonsInOneLine();
        blankLinesKeepNumbering();
        missingFile();
        nullPath();
        emptyFile();
        headerOnly();
        brokenEncoding();
        resultListsAreCopies();
        System.out.println("Пройдено: " + passed + ", провалено: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static Path file(String... lines) throws IOException {
        Path path = Files.createTempFile("cl", ".csv");
        Files.writeString(path, String.join("\n", lines), StandardCharsets.UTF_8);
        path.toFile().deleteOnExit();
        return path;
    }

    private static ClientImporter importer(ClientRepository repository) {
        return new ClientImporter(new ClientCsvParser(), new ClientValidator(), repository);
    }

    private static ImportResult run(String... lines) throws Exception {
        return importer(new ClientRepository()).importFile(file(lines));
    }

    private static ImportResult runWithHeader(String row) throws Exception {
        return run(HEADER, row);
    }

    private static void typicalFile() throws Exception {
        ImportResult r = run(HEADER,
                "7701234567;Иванов Иван;ivan@mail.ru;+7 900 123-45-67",
                "770123456789;ООО Ромашка;info@romashka.ru;8 (495) 123-45-67");
        check("типовой файл: 2 принято, 0 отклонено", r.getAccepted() == 2 && r.getRejected() == 0);
    }

    private static void innLengthBoundaries() throws Exception {
        boolean ten = runWithHeader("1234567890;A;a@b;9001234567").getAccepted() == 1;
        boolean twelve = runWithHeader("123456789012;A;a@b;9001234567").getAccepted() == 1;
        boolean nine = runWithHeader("123456789;A;a@b;9001234567").getRejected() == 1;
        boolean eleven = runWithHeader("12345678901;A;a@b;9001234567").getRejected() == 1;
        boolean thirteen = runWithHeader("1234567890123;A;a@b;9001234567").getRejected() == 1;
        check("ИНН: 10 и 12 цифр принимаются, 9, 11 и 13 отклоняются", ten && twelve && nine && eleven && thirteen);
    }

    private static void innFormat() throws Exception {
        boolean letters = runWithHeader("12345abcde;A;a@b;9001234567").getRejected() == 1;
        boolean spaces = runWithHeader("1234 56789;A;a@b;9001234567").getRejected() == 1;
        boolean minus = runWithHeader("-123456789;A;a@b;9001234567").getRejected() == 1;
        check("ИНН с буквами, пробелом и минусом отклоняется", letters && spaces && minus);
    }

    private static void emailRules() throws Exception {
        boolean valid = runWithHeader("1234567890;A;a@b;9001234567").getAccepted() == 1;
        boolean noAt = runWithHeader("1234567890;A;ab.ru;9001234567").getRejected() == 1;
        boolean twoAt = runWithHeader("1234567890;A;a@@b;9001234567").getRejected() == 1;
        boolean twoAtApart = runWithHeader("1234567890;A;a@b@c;9001234567").getRejected() == 1;
        boolean emptyLeft = runWithHeader("1234567890;A;@b.ru;9001234567").getRejected() == 1;
        boolean emptyRight = runWithHeader("1234567890;A;a@;9001234567").getRejected() == 1;
        boolean onlyAt = runWithHeader("1234567890;A;@;9001234567").getRejected() == 1;
        check("email: ровно один @ с непустыми частями", valid && noAt && twoAt && twoAtApart && emptyLeft
                && emptyRight && onlyAt);
    }

    private static void phoneIsOptional() throws Exception {
        boolean empty = runWithHeader("1234567890;A;a@b;").getAccepted() == 1;
        boolean any = runWithHeader("1234567890;A;a@b;любой текст").getAccepted() == 1;
        check("телефон не обязателен и не проверяется", empty && any);
    }

    private static void emptyFields() throws Exception {
        boolean noInn = runWithHeader(";A;a@b;9001234567").getRejected() == 1;
        boolean noName = runWithHeader("1234567890; ;a@b;9001234567").getRejected() == 1;
        boolean noEmail = runWithHeader("1234567890;A;;9001234567").getRejected() == 1;
        check("пустые поля: ИНН, имя, email", noInn && noName && noEmail);
    }

    private static void wrongFieldCount() throws Exception {
        boolean few = runWithHeader("1234567890;A;a@b").getRejected() == 1;
        boolean many = runWithHeader("1234567890;A;a@b;9001234567;extra").getRejected() == 1;
        check("неверное число полей", few && many);
    }

    private static void duplicateReplacesPrevious() throws Exception {
        ClientRepository repository = new ClientRepository();
        ImportResult r = importer(repository).importFile(file(HEADER,
                "1234567890;Первый;one@mail.ru;9001234567",
                "1234567890;Второй;two@mail.ru;9007654321"));
        boolean last = repository.findByInn("1234567890").map(c -> c.getName().equals("Второй")).orElse(false);
        check("дубликат ИНН: последняя запись заменяет прежнюю",
                r.getAccepted() == 2 && r.getReplaced() == 1 && r.getRejected() == 0 && repository.size() == 1
                        && last);
    }

    private static void duplicateReplacementLimit() throws Exception {
        int limit = ValidationRules.MAX_DUPLICATE_REPLACEMENTS;
        String[] lines = new String[limit + 3];
        lines[0] = HEADER;
        lines[1] = "1234567890;N0;a@b;1";
        for (int i = 1; i <= limit + 1; i++) {
            lines[i + 1] = "1234567890;N" + i + ";a@b;1";
        }
        ClientRepository repository = new ClientRepository();
        ImportResult r = importer(repository).importFile(file(lines));
        boolean counts = r.getReplaced() == limit && r.getAccepted() == limit + 1 && r.getRejected() == 1;
        boolean line = r.getErrors().get(0).getLineNumber() == limit + 3;
        boolean kept = repository.findByInn("1234567890").map(c -> c.getName().equals("N" + limit)).orElse(false);
        check("дубликаты: 35 замен разрешено, 36-я отклонена, последняя принятая запись сохранена",
                counts && line && kept);
    }

    private static void invalidDuplicateDoesNotReplace() throws Exception {
        ClientRepository repository = new ClientRepository();
        ImportResult r = importer(repository).importFile(file(HEADER,
                "1234567890;Первый;one@mail.ru;1",
                "1234567890;Второй;broken-email;1"));
        boolean kept = repository.findByInn("1234567890").map(c -> c.getName().equals("Первый")).orElse(false);
        check("некорректный дубликат не заменяет прежнюю запись",
                r.getAccepted() == 1 && r.getReplaced() == 0 && r.getRejected() == 1 && kept);
    }

    private static void importContinuesAfterError() throws Exception {
        ClientRepository repository = new ClientRepository();
        ImportResult r = importer(repository).importFile(file(HEADER,
                "1234567890;A;a@b;9001234567",
                "bad line",
                "1234567891;B;b@b;9001234567",
                "12;C;c@b;9001234567",
                "1234567892;D;d@b;9001234567"));
        boolean lines = r.getErrors().get(0).getLineNumber() == 3 && r.getErrors().get(1).getLineNumber() == 5;
        check("ошибка строки не останавливает импорт, номера строк верны",
                r.getAccepted() == 3 && r.getRejected() == 2 && repository.size() == 3 && lines);
    }

    private static void severalReasonsInOneLine() throws Exception {
        ImportResult r = runWithHeader("12;;ab;12");
        String reason = r.getErrors().get(0).getReason();
        check("в одной строке собираются все причины", r.getRejected() == 1 && reason.split(";").length == 3);
    }

    private static void blankLinesKeepNumbering() throws Exception {
        ImportResult r = run(HEADER, "", "   ", "bad");
        check("пустые строки пропускаются, нумерация сохраняется",
                r.getAccepted() == 0 && r.getRejected() == 1 && r.getErrors().get(0).getLineNumber() == 4);
    }

    private static void missingFile() {
        boolean thrown = false;
        try {
            importer(new ClientRepository()).importFile(Path.of("no_such_dir/no_such_file.csv"));
        } catch (ImportFileException e) {
            thrown = true;
        }
        check("отсутствующий файл завершает импорт ImportFileException", thrown);
    }

    private static void nullPath() {
        boolean thrown = false;
        try {
            importer(new ClientRepository()).importFile(null);
        } catch (ImportFileException e) {
            thrown = true;
        }
        check("путь null завершает импорт ImportFileException", thrown);
    }

    private static void emptyFile() throws Exception {
        boolean thrown = false;
        try {
            importer(new ClientRepository()).importFile(file());
        } catch (ImportFileException e) {
            thrown = true;
        }
        check("пустой файл без заголовка завершает импорт", thrown);
    }

    private static void headerOnly() throws Exception {
        ImportResult r = run(HEADER);
        check("файл только с заголовком: 0 принято, 0 отклонено", r.getAccepted() == 0 && r.getRejected() == 0);
    }

    private static void brokenEncoding() throws Exception {
        Path path = Files.createTempFile("cl", ".csv");
        path.toFile().deleteOnExit();
        Files.write(path, new byte[] {'h', '\n', (byte) 0xFF, (byte) 0xFE, '\n'});
        boolean thrown = false;
        try {
            importer(new ClientRepository()).importFile(path);
        } catch (ImportFileException e) {
            thrown = true;
        }
        check("ошибка чтения файла (неверная кодировка) завершает импорт", thrown);
    }

    private static void resultListsAreCopies() throws Exception {
        ClientRepository repository = new ClientRepository();
        ImportResult r = importer(repository).importFile(file(HEADER, "bad"));
        boolean errorsImmutable = false;
        try {
            r.getErrors().clear();
        } catch (UnsupportedOperationException e) {
            errorsImmutable = true;
        }
        repository.findAll().clear();
        check("списки результата и репозитория наружу копиями", errorsImmutable && r.getRejected() == 1);
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }
}
