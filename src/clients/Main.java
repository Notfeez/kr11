package clients;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path path = Path.of(args.length > 0 ? args[0] : "data/sample.csv");
        ClientRepository repository = new ClientRepository();
        ClientImporter importer = new ClientImporter(new ClientCsvParser(), new ClientValidator(), repository);
        try {
            ImportResult result = importer.importFile(path);
            System.out.println(result.report());
            System.out.println("В репозитории клиентов: " + repository.size());
            for (Client client : repository.findAll()) {
                System.out.println(client);
            }
        } catch (ImportFileException e) {
            System.out.println("Импорт прекращён: " + e.getMessage());
            System.exit(1);
        }
    }
}
