package src;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class LoginGUI implements ActionListener
{
    private static JLabel usernameLabel;
    private static JTextField userText;
    private static JLabel passwordLabel;
    private static JPasswordField passwordText;
    private static JButton loginButton;
    private static JButton signUpButton;
    public static void main(String[] args) {
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
        
        loginButton = new JButton("Login");
        loginButton.setBounds(80,95,80,25);
        loginButton.addActionListener(new LoginGUI());
        panel.add(loginButton);
        
        signUpButton = new JButton("Sign Up");
        signUpButton.setBounds(180,95,80,25);
        panel.add(signUpButton);
        
        frame.setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String username = userText.getText();
        String password = passwordText.getText();
        System.out.println(username + ", " + password);
    }
}
