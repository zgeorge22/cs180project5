import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 * LoginGUI is a class designed to present a simple login system for
 * a user that wants to access our messaging program. This GUI offers
 * the user the ability to log in with an existing account or create a
 * new one, and will present an error message if the potential credentials
 * do not meet the required criteria/
 * 
 * Author (Jack Dorkin)
 */
public class LoginGUI implements ActionListener
{
    //All of the labels, text boxes, and buttons that will be used in the
    //GUI for logging in and the one for account creation
    private static JLabel usernameLabel;
    private static JTextField userText;
    private static JLabel passwordLabel;
    private static JPasswordField passwordText;
    private static JButton loginButton;
    private static JButton signUpButton;
    private static JButton createAccountButton;
    
    /** Main method, defines majority of fields, sets up frame and panel
    * that will be used for the initial login popup
    */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        JPanel signUpPanel = new JPanel();
        frame.setSize(350,200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        panel.setLayout(null);
        
        usernameLabel = new JLabel("Username");
        usernameLabel.setBounds(10,20,80,25);
        panel.add(usernameLabel);
        
        userText = new JTextField();
        userText.setBounds(100,20,165,25);
        panel.add(userText);
        
        passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(10,50,80,25);
        panel.add(passwordLabel);
        
        passwordText = new JPasswordField();
        passwordText.setBounds(100,50,165,25);
        panel.add(passwordText);
        
        loginButton = new JButton("Login");
        loginButton.setBounds(80,95,80,25);
        loginButton.addActionListener(new LoginGUI());
        panel.add(loginButton);
        
        signUpButton = new JButton("Sign Up");
        signUpButton.setBounds(180,95,80,25);
        signUpButton.addActionListener(new LoginGUI());
        panel.add(signUpButton);
        
        frame.setVisible(true);
    }
    
    /** Override of the actionPerformed method from the ActionListener interface
    * Provides action descriptions for all available buttons in the code
    * Creates a new GUI upon being prompted with the "Sign Up" button
    * on the initial login GUI. Prompts error popups if user uses incorrect
    * criteria for username and/or password.
    */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == loginButton) {
            String username = userText.getText();
            String password = passwordText.getText();
            System.out.println(username + ", " + password);
        }
        else if(e.getSource() == signUpButton) {
            JFrame frame = new JFrame();
            JPanel panel = new JPanel();
            frame.setSize(350,200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            panel.setLayout(null);
            
            usernameLabel = new JLabel("Username");
            usernameLabel.setBounds(10,20,80,25);
            panel.add(usernameLabel);
        
            userText = new JTextField();
            userText.setBounds(100,20,165,25);
            panel.add(userText);
        
            passwordLabel = new JLabel("Password");
            passwordLabel.setBounds(10,50,80,25);
            panel.add(passwordLabel);
        
            passwordText = new JPasswordField();
            passwordText.setBounds(100,50,165,25);
            panel.add(passwordText);
            
            createAccountButton = new JButton("Create Account");
            createAccountButton.setBounds(100,95,140,25);
            createAccountButton.addActionListener(new LoginGUI());
            panel.add(createAccountButton);
            
            frame.setVisible(true);
        } else if(e.getSource() == createAccountButton) {
            JFrame frame = new JFrame();
            JPanel panel = new JPanel();
            frame.setSize(350,200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            panel.setLayout(null);
            int count = 0;
            for(int i = 0; i < userText.getText().length(); i++) {
                if(!Character.isLetterOrDigit(userText.getText().charAt(i))) {
                    count++;
                    break;
                }
            }
            if(count == 0) {
                for(int i = 0; i < passwordText.getText().length(); i++) {
                    if(!Character.isLetterOrDigit(passwordText.getText().charAt(i))) {
                        count++;
                        break;
                    }
                }
            }
            if(count != 0) {
                JOptionPane.showMessageDialog(panel, "Error: Non-alphanumeric character used in username or password.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "Account created with username " + userText.getText() + " and password " + passwordText.getText(), "Account Created", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
