import javax.swing.*;
import java.awt.event.*;

/**
 * This class contains the GUI for the Login Window. This class is designed to
 * present a simple login system for a user that wants to access our messaging
 * program. This GUI offers the user the ability to log in with an existing
 * account or create a new one, and will present an error message if the
 * potential credentials do not meet the required criteria
 *
 * <p>
 * Purdue University -- CS18000 -- Spring 2021 -- Project 5
 * </p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

public class LoginWindow extends JFrame {

    private Client client;

    // All of the labels, text boxes, and buttons that will be used in the
    // GUI for logging in and the one for account creation
    private JLabel usernameLabel;
    private JTextField usernameText;
    private JLabel passwordLabel;
    private JTextField passwordText;
    private JButton loginButton;
    private JButton signUpButton;

    // Creates a new GUI upon being prompted with the "Sign Up" button
    // on the initial login GUI. Prompts error popups if user uses incorrect
    // criteria for username and/or password.
    public LoginWindow(Client client) {
        super("Chat Login");

        this.client = client;

        initializeComponents();

        setSize(350, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                client.requestCancelLogin();
            }
        });
        setVisible(true);
    }

    // Sets up all GUI compoentns in the desired layout. Initializes any event
    // listeners for buttons or key strokes.
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
                if (Client.validUsername(usernameText.getText()) && Client.validPassword(passwordText.getText())) {
                    client.requestLoginAccount(usernameText.getText(), passwordText.getText());
                }
            }
        });
        loginButton.setBounds(80, 95, 80, 25);
        panel.add(loginButton);

        signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Client.validUsername(usernameText.getText()) && Client.validPassword(passwordText.getText())) {
                    client.requestCreateAccount(usernameText.getText(), passwordText.getText());
                }
            }
        });
        signUpButton.setBounds(180, 95, 80, 25);
        panel.add(signUpButton);

        add(panel);
    }
}
