import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerProcess extends Thread {

    private final Socket clientSocket;

    public ServerProcess(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            clientProcess();
        } catch (IOException | AccountNotExistException e) {
            e.printStackTrace();
        }
    }

    private void clientProcess() throws IOException, AccountNotExistException {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());

        String loginOption = reader.readLine();
        boolean loggedIn = false;
        Account userAccount = new Account("PH", "PH");
        if (loginOption.equals("0")) {
            String username = reader.readLine();
            String password = reader.readLine();
            userAccount = accountLogin(username, password);
            loggedIn = true;
        } else if (loginOption.equals("1")) {
            String username = reader.readLine();
            String password = reader.readLine();
            userAccount.setUsername(username);
            userAccount.setPassword(password);
            loggedIn = true;
        }

        if (loggedIn && !userAccount.getUsername().equals("PH") && !userAccount.getPassword().equals("PH")) {
            pw.write("User logged in " + userAccount.getUsername());
            pw.println();
            pw.flush();
            ServerBackground.addUser(userAccount);
        }

        clientSocket.close();
        ServerBackground.removeUser(userAccount);
    }

    private Account accountLogin(String username, String password) throws AccountNotExistException {

        Account checkAccount = Database.getAccountByUsername(username);
        if (!password.equals(checkAccount.getPassword())) {
            //TODO re-enter password
        }
        return checkAccount;
    }

    public Account createAccount(String username, String password) {
        return new Account(username, password);
    }
}
