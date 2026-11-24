package clients;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientRepository {
    private final Map<String, Client> clients = new LinkedHashMap<>();

    public boolean save(Client client) {
        return clients.put(client.getInn(), client) != null;
    }

    public Optional<Client> findByInn(String inn) {
        return Optional.ofNullable(clients.get(inn));
    }

    public List<Client> findAll() {
        return new ArrayList<>(clients.values());
    }

    public int size() {
        return clients.size();
    }
}
