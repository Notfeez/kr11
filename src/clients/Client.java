package clients;

public class Client {
    private final String inn;
    private final String name;
    private final String email;
    private final String phone;

    public Client(String inn, String name, String email, String phone) {
        this.inn = inn;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getInn() {
        return inn;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return inn + " | " + name + " | " + email + " | " + phone;
    }
}
