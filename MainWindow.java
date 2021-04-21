import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListenerProxy;
import java.util.ArrayList;
import java.util.List;

public class MainWindow {

    // private Conversation[] conversations;
    // private Conversation

    public MainWindow() {
        JFrame frame = new JFrame("Chat");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());

        JPanel sidePanel = new JPanel();
        JPanel chatPanel = new JPanel();

        // SIDE PANEL LAYOUT
        sidePanel.setLayout(new BorderLayout());
        sidePanel.add(new JButton("Create Conversation"), BorderLayout.NORTH);

        // ROUGH CODE LOL
        List<String> testList = new ArrayList<>(10);
        for (int i = 0; i < 20; i++) {
            testList.add(i + "Zach, Rishi, Jack, Natalie, Ben, and more");
        }

        JList<String> list = new JList<String>(testList.toArray(new String[testList.size()]));
        list.setCellRenderer(new ChatListCellRenderer());
        list.setFixedCellWidth(250);
        // list.setFixedCellHeight(50);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        sidePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel botPanel = new JPanel();
        botPanel.add(new JButton("Account"));
        botPanel.add(new JButton("Sign Out"));
        sidePanel.add(botPanel, BorderLayout.SOUTH);

        // CHAT PANEL LAYOUT
        chatPanel.setLayout(new BorderLayout());

        JPanel composePanel = new JPanel();
        composePanel.setLayout(new BorderLayout());
        composePanel.add(new JTextField(), BorderLayout.CENTER);
        composePanel.add(new JButton("Send"), BorderLayout.EAST);
        chatPanel.add(composePanel, BorderLayout.SOUTH);

        chatPanel.setBackground(Color.gray);

        content.add(sidePanel, BorderLayout.WEST);
        content.add(chatPanel, BorderLayout.CENTER);

        frame.setSize(650, 500);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    class ChatListCellRenderer extends JLabel implements ListCellRenderer<Object> {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            String s = value.toString();
            int i = Integer.parseInt(s.substring(0, 1));
            s = s.substring(1);

            setText("<html>Conversation: " + i + "<br/>&nbsp;&nbsp;" + s);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow();
            }
        });
    }
}
