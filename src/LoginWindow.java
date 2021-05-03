import javax.swing.*;

import java.awt.event.*;

public class LoginWindow extends JFrame {

    private Client client;

    private JLabel usernameLabel;
    private JTextField usernameText;
    private JLabel passwordLabel;
    private JTextField passwordText;
    private JButton loginButton;
    private JButton signUpButton;

    public LoginWindow(Client client) {
        super("Chat Login");

        this.client = client;

        initializeComponents();

        setSize(350, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // addWindowListener(new WindowAdapter() {
        // @Override
        // public void windowClosing(WindowEvent e) {
        // super.windowClosing(e);
        // client.requestLogoutAccount();
        // }
        // });
        setVisible(true);
    }

    private void initializeComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        usernameLabel = new JLabel("Username");
        usernameLabel.setBounds(10, 20, 80, 25);
        panel.add(usernameLabel);

        usernameText = new JTextField();
        usernameText.setBounds(100, 20, 165, 25);
        panel.add(usernameText);

        passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        passwordText = new JTextField();
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.requestLoginAccount(usernameText.getText(), passwordText.getText());
            }
        });
        loginButton.setBounds(80, 95, 80, 25);
        panel.add(loginButton);

        signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.requestCreateAccount(usernameText.getText(), passwordText.getText());
            }
        });
        signUpButton.setBounds(180, 95, 80, 25);
        panel.add(signUpButton);

        add(panel);
    }
}