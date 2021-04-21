import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListenerProxy;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {

    private JList<Conversation> chatList;

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

        JButton createChatButton = new JButton("Create New Chat");
        createChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // UPDATE test button functionality
                // addNewChat("0Created chat");

                JOptionPane.showMessageDialog(MainWindow.this, "Button Pressed", "Hey",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        sidePanel.add(createChatButton, BorderLayout.NORTH);

        chatList = new JList<Conversation>();
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setCellRenderer(new ChatListCellRenderer());
        chatList.setFixedCellWidth(250); // .setFixedCellHeight(50);

        JScrollPane scrollPane = new JScrollPane(chatList);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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

    private DefaultListModel<Conversation> getChatEntities() {
        ListModel<Conversation> chatEntities = chatList.getModel();
        if (!(chatEntities instanceof DefaultListModel)) {
            DefaultListModel<Conversation> defaultChatEntries = new DefaultListModel<Conversation>();
            chatList.setModel(defaultChatEntries);
            return defaultChatEntries;
        }
        return (DefaultListModel<Conversation>) chatEntities;
    }

    public void setChatList(ArrayList<Conversation> conversationList) {
        DefaultListModel<Conversation> chatEntities = getChatEntities();
        for (Conversation convo : conversationList) {
            chatEntities.addElement(convo);
        }
    }

    public void addNewChat(Conversation convo) {
        DefaultListModel<Conversation> chatEntities = getChatEntities();
        chatEntities.addElement(convo);
    }

    public void updateChatEntry(Conversation conversation) {
        DefaultListModel<Conversation> chatEntities = getChatEntities();
        for (int i = 0; i < chatEntities.size(); i++) {
            Conversation chatEntry = chatEntities.getElementAt(i);
            if (chatEntry.getConversationId() == conversation.getConversationId()) {
                chatEntities.setElementAt(conversation, i);
                break;
            }
        }
    }

    class ChatListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // UPDATE string parsing to proper conversation parsing
            Conversation conversation = (Conversation) value;
            int id = conversation.getConversationId();
            String participants = conversation.getParticipantsString();
            setText("<html>Conversation: " + id + "<br/>&nbsp;&nbsp;" + participants);

            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Database db = new Database();

                Account z = new Account("Zach", "help");
                Account r = new Account("Rishi", "1234");
                Account j = new Account("Jack", "password");
                Account b = new Account("Ben", "qwerty");
                Account n = new Account("Natalie", "asdf");

                ArrayList<Account> accountsListSmall = new ArrayList<>();
                accountsListSmall.add(z);
                accountsListSmall.add(r);

                ArrayList<Account> accountsListMedium = new ArrayList<>();
                accountsListMedium.add(z);
                accountsListMedium.add(r);
                accountsListMedium.add(j);
                accountsListMedium.add(b);

                ArrayList<Account> accountsListLarge = new ArrayList<>();
                accountsListLarge.add(z);
                accountsListLarge.add(r);
                accountsListLarge.add(j);
                accountsListLarge.add(b);
                accountsListLarge.add(n);

                Conversation chatSmall = new Conversation(accountsListSmall);
                Conversation chatMedium = new Conversation(accountsListMedium);
                Conversation chatLarge = new Conversation(accountsListLarge);

                ArrayList<Conversation> conversationList = new ArrayList<Conversation>();
                conversationList.add(chatSmall);
                conversationList.add(chatLarge);

                // TEST MAIN WINDOW
                MainWindow mw = new MainWindow();

                // will be used once a conversations list is received from the server
                mw.setChatList(conversationList);

                // will be used upon new chat
                mw.addNewChat(chatMedium);

                // will be used for participant changes (leaving chats)
                try {
                    b.removeConversation(chatMedium.getConversationId());
                } catch (ConversationNotFoundException e) {
                    System.out.println("Could not remove conversation!");
                }
                mw.updateChatEntry(chatMedium);
            }
        });
    }
}
