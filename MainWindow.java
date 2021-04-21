import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListenerProxy;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {

    private JList<ChatEntry> chatList;

    public MainWindow() {
        super("Chat");

        initializeComponents();

        setSize(650, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    protected void initializeComponents() {
        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        // SIDE PANEL LAYOUT
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout());
        sidePanel.add(new JButton("Create Conversation"), BorderLayout.NORTH);

        chatList = new JList<>();
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setCellRenderer(new ChatListCellRenderer());
        chatList.setFixedCellWidth(250); // .setFixedCellHeight(50);

        JScrollPane scrollPane = new JScrollPane(chatList);
        sidePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel botPanel = new JPanel();
        botPanel.add(new JButton("Account"));
        botPanel.add(new JButton("Sign Out"));
        sidePanel.add(botPanel, BorderLayout.SOUTH);

        // CHAT PANEL LAYOUT
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBackground(Color.gray);
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel composePanel = new JPanel();
        composePanel.setLayout(new BorderLayout());
        composePanel.add(new JTextField(), BorderLayout.CENTER);
        composePanel.add(new JButton("Send"), BorderLayout.EAST);
        chatPanel.add(composePanel, BorderLayout.SOUTH);

        content.add(sidePanel, BorderLayout.WEST);
        content.add(chatPanel, BorderLayout.CENTER);
    }

    private DefaultListModel<ChatEntry> getChatEntities() {
        ListModel<ChatEntry> chatEntities = chatList.getModel();
        if (!(chatEntities instanceof DefaultListModel)) {
            DefaultListModel<ChatEntry> defaultChatEntries = new DefaultListModel<>();
            chatList.setModel(defaultChatEntries);
            return defaultChatEntries;
        }
        return (DefaultListModel<ChatEntry>) chatEntities;
    }

    // UPDATE to be conversations instead of strings
    public void setChatList(List<String> chatContentList) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (String content : chatContentList) {
            chatEntities.addElement(new ChatEntry(content));
        }
    }

    // UPDATE all instances of string in chat entry to be a conversation object
    public void addNewChat(String newChat) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        chatEntities.addElement(new ChatEntry(newChat));
    }

    // UPDATE all instances of string in chat entry to be a conversation object
    public void updateChatEntry(String conversation) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (int i = 0; i < chatEntities.size(); i++) {
            ChatEntry chatEntry = chatEntities.getElementAt(i);
            if (chatEntry.contains(conversation)) {
                chatEntities.setElementAt(new ChatEntry(conversation + " [update]"), i);
                break;
            }
        }
    }

    // UPDATE to add conversation functionality instead of String
    class ChatEntry {
        private String content;

        public ChatEntry(String content) {
            this.content = content;
        }

        public String getContent() {
            return this.content;
        }

        @Override
        public String toString() {
            return getContent();
        }

        // UPDATE to compare conversation IDs
        public boolean contains(String s) {
            return this.content.equals(s);
        }
    }

    class ChatListCellRenderer extends JLabel implements ListCellRenderer<Object> {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            // UPDATE string parsing to proper conversation parsing
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
                MainWindow mw = new MainWindow();

                List<String> testList = new ArrayList<>(10);
                for (int i = 0; i < 8; i++) {
                    testList.add(i + "Zach, Rishi, Jack, Natalie, Ben, and more");
                }

                // will be used once a conversations list is received from the server
                mw.setChatList(testList);

                // will be used upon new chat

                mw.addNewChat("9Another chat");

                // will be used for participant changes (leaving chats)
                mw.updateChatEntry("9Another chat");
            }
        });
    }
}
