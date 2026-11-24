package clients;

public class ClientDraft {
    private final int lineNumber;
    private final String inn;
    private final String name;
    private final String email;
    private final String phone;

    public ClientDraft(int lineNumber, String inn, String name, String email, String phone) {
        this.lineNumber = lineNumber;
        this.inn = inn == null ? "" : inn.trim();
        this.name = name == null ? "" : name.trim();
        this.email = email == null ? "" : email.trim();
        this.phone = phone == null ? "" : phone.trim();
    }

    public int getLineNumber() {
        return lineNumber;
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

    public Client toClient() {
        return new Client(inn, name, email, phone);
    }
}
