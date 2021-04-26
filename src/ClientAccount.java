public class ClientAccount {

    private String username;
    private String password;

    public ClientAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public ClientAccount(Account account) {
        this.username = account.getUsername();
        this.password = account.getPassword();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
