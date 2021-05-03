import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class MainWindow extends JFrame {

    private Client client;

    private Container content;
    private JPanel sidePanel;
    private JPanel sideHeader;
    private JButton createChatButton;
    private JButton leaveChatButton;
    private JList<ChatEntry> chatList;
    private JScrollPane chatListScrollPane;
    private JButton importChatButton;
    private JButton exportChatButton;
    private JButton accountButton;
    private JButton signOutButton;
    private JPanel botPanel;
    private JPanel chatPanel;
    private JPanel convoHeader;
    private JTextField participantsField;
    private JList<MsgEntry> msgList;
    private JScrollPane msgListScrollPane;
    private JPanel composeBar;
    private JScrollPane composeScrollPane;
    private JTextArea composeMessage;
    private JComboBox<String> messageActions;
    private JSplitPane splitPane;
    private final String SEND_ACTION = "SEND";
    private final String EDIT_ACTION = "EDIT";
    private final String DELETE_ACTION = "DELETE";

    private ChatEntry currentChat;
    private MsgEntry currentMsg;

    public MainWindow(Client client) {
        super("Chat Window");

        this.client = client;

        initializeComponents();

        setSize(650, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                client.requestLogoutAccount();
            }
        });
        setVisible(true);

        participantsField.requestFocusInWindow();
    }

    protected void initializeComponents() {
        content = getContentPane();
        content.setLayout(new BorderLayout());

        // SIDE PANEL LAYOUT
        createChatButton = new JButton("Create Chat");
        createChatButton.setForeground(Color.decode("#35A437"));

        createChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideMsgList();
            }
        });

        leaveChatButton = new JButton("Leave Chat");
        leaveChatButton.setForeground(Color.decode("#FF3E31"));
        leaveChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentChat != null) {
                    if (client.requestLeaveConvo(currentChat.getConversation())) {
                        hideMsgList();
                    } else {
                        JOptionPane.showMessageDialog(null, "Unable to leave chat!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No chat selected to leave!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        sideHeader = new JPanel();
        sideHeader.setLayout(new GridLayout(1, 2));
        sideHeader.add(createChatButton);
        sideHeader.add(leaveChatButton);

        chatList = new JList<ChatEntry>();
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setCellRenderer(new ChatListCellRenderer());
        chatList.setFixedCellWidth(250); // .setFixedCellHeight(50);
        chatList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (chatList.getSelectedValue() != null) {
                        currentChat = chatList.getSelectedValue();
                        showMsgList();
                        splitPane.resetToPreferredSizes();
                        composeMessage.setText("");
                        composeMessage.requestFocusInWindow();
                    }
                }
            }
        });

        chatListScrollPane = new JScrollPane(chatList);
        chatListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        importChatButton = new JButton("Import Chat(s)");
        importChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Import selected chats!
            }
        });

        exportChatButton = new JButton("Export Chat(s)");
        exportChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Export selected chats!
            }
        });

        accountButton = new JButton("Account");
        accountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = JOptionPane.showInputDialog("Enter new password: ");

                if (validPassword(password)) {
                    client.requestEditPassword(password);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid password!", "Warning", JOptionPane.WARNING_MESSAGE);
                }

            }
        });

        signOutButton = new JButton("Sign Out");
        signOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.requestLogoutAccount();
            }
        });

        botPanel = new JPanel();
        botPanel.setLayout(new GridLayout(2, 2));
        botPanel.add(importChatButton);
        botPanel.add(accountButton);
        botPanel.add(exportChatButton);
        botPanel.add(signOutButton);

        sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout());
        sidePanel.add(sideHeader, BorderLayout.NORTH);
        sidePanel.add(chatListScrollPane, BorderLayout.CENTER);
        sidePanel.add(botPanel, BorderLayout.SOUTH);

        // CHAT PANEL LAYOUT
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        participantsField = new JTextField();

        convoHeader = new JPanel();
        convoHeader.setLayout(new BorderLayout());
        convoHeader.add(new JLabel(" Participants: "), BorderLayout.WEST);
        convoHeader.add(participantsField, BorderLayout.CENTER);
        chatPanel.add(convoHeader, BorderLayout.NORTH);

        msgList = new JList<MsgEntry>();
        msgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        msgList.setCellRenderer(new MsgListCellRenderer());

        msgListScrollPane = new JScrollPane(msgList);
        msgListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        composeBar = new JPanel();
        composeBar.setLayout(new BorderLayout());
        composeMessage = new JTextArea();
        composeMessage.setLineWrap(true);
        composeMessage.setWrapStyleWord(true);
        composeScrollPane = new JScrollPane(composeMessage);
        composeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        composeBar.add(composeScrollPane, BorderLayout.CENTER);

        InputMap inputMap = composeMessage.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = composeMessage.getActionMap();
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        inputMap.put(enterKey, enterKey.toString());
        actionMap.put(enterKey.toString(), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptMessageAction();
            }
        });

        messageActions = new JComboBox<String>();
        messageActions.addItem(SEND_ACTION);
        messageActions.addItem(EDIT_ACTION);
        messageActions.addItem(DELETE_ACTION);
        messageActions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedAction = (String) messageActions.getSelectedItem();

                switch (selectedAction) {
                    case SEND_ACTION:
                        currentMsg = null;
                        break;
                    case EDIT_ACTION:
                        chatListScrollPane.revalidate();
                        break;
                    case DELETE_ACTION:
                        break;
                    default:
                        System.out.println("ERROR: invalid action attempted!"); // should never happen!
                }
            }
        });

        composeBar.add(messageActions, BorderLayout.EAST);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, msgListScrollPane, composeBar);
        splitPane.setResizeWeight(1);
        chatPanel.add(splitPane, BorderLayout.CENTER);

        content.add(sidePanel, BorderLayout.WEST);
        content.add(chatPanel, BorderLayout.CENTER);
    }

    public void attemptMessageAction() {
        String selectedAction = (String) messageActions.getSelectedItem();

        switch (selectedAction) {
            case SEND_ACTION:
                if (currentChat != null) {
                    // Using chat...
                    if (!composeMessage.getText().equals("")) {
                        if (client.requestCreateMsg(currentChat.getConversation(), composeMessage.getText())) {
                            composeMessage.setText("");
                            composeMessage.requestFocusInWindow();
                        } else {
                            JOptionPane.showMessageDialog(null, "Unable to send message!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "No message to send!", "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    // Creating chat...
                    if (!participantsField.getText().equals("")) {
                        if (!composeMessage.getText().equals("")) {
                            if (client.requestCreateConvo(participantsField.getText(), composeMessage.getText())) {
                                participantsField.setText("");
                                composeMessage.setText("");
                                chatList.setSelectedIndex(0);
                            } else {
                                JOptionPane.showMessageDialog(null, "Unable to send message!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "No message to send!", "Warning",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "No participants entered!", "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
                break;
            case EDIT_ACTION:
                if (currentMsg != null) {
                    if (currentMsg.getMessage().getSender().equals(client.getUsername())) {
                        if (!composeMessage.getText().equals("")) {
                            if (client.requestEditMsg(currentChat.getConversation(), currentMsg.getMessage(),
                                    composeMessage.getText())) {
                                currentMsg = null;
                                msgList.setSelectedValue(null, false);
                                composeMessage.setText("");
                                composeMessage.requestFocusInWindow();
                            } else {
                                JOptionPane.showMessageDialog(null, "Unable to edit message!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "No message to send!", "Warning",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "You can only edit messages that you have created!",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No message selected to edit!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                break;
            case DELETE_ACTION:
                if (currentMsg != null) {
                    if (currentMsg.getMessage().getSender().equals(client.getUsername())) {
                        if (client.requestDeleteMsg(currentChat.getConversation(), currentMsg.getMessage())) {
                            currentMsg = null;
                            msgList.setSelectedValue(null, false);
                            composeMessage.setText("");
                        } else {
                            JOptionPane.showMessageDialog(null, "Unable to delete message!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "You can only delete messages that you have created!",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No message selected to delete!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                break;
            default:
                System.out.println("ERROR: invalid action attempted!"); // should never happen!
        }
    }

    private boolean validPassword(String password) {
        // UPDATE LATER
        System.out.println(password);

        return true;
    }

    private DefaultListModel<ChatEntry> getChatEntities() {
        ListModel<ChatEntry> chatEntities = chatList.getModel();
        if (!(chatEntities instanceof DefaultListModel)) {
            DefaultListModel<ChatEntry> defaultChatEntries = new DefaultListModel<ChatEntry>();
            chatList.setModel(defaultChatEntries);
            return defaultChatEntries;
        }
        return (DefaultListModel<ChatEntry>) chatEntities;
    }

    public void setChatList(ArrayList<Conversation> conversations) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (Conversation convo : conversations) {
            chatEntities.addElement(new ChatEntry(convo, false));
        }
    }

    public void addNewChat(Conversation convo) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        ChatEntry newChat = new ChatEntry(convo, true);
        chatEntities.add(0, newChat);
    }

    // UPDATE LATER, probably shouldnt update a conversation, sort the list, and
    // redisplay the messages all in one function
    public void updateChatEntry(Conversation convo) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (int i = 0; i < chatEntities.size(); i++) {
            ChatEntry chatEntry = chatEntities.getElementAt(i);
            if (chatEntry.getConversation().getConversationId() == convo.getConversationId()) {
                chatEntities.setElementAt(new ChatEntry(convo, true), i);
                break;
            }
        }

        sortChatEntries();

        if (currentChat != null) {
            if (currentChat.getConversation().getConversationId() == convo.getConversationId()) {
                showMsgList();
            }
        }
    }

    public void removeChatEntry(Conversation convo) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (int i = 0; i < chatEntities.size(); i++) {
            ChatEntry chatEntry = chatEntities.getElementAt(i);
            if (chatEntry.getConversation().getConversationId() == convo.getConversationId()) {
                chatEntities.remove(i);
                break;
            }
        }
    }

    public void sortChatEntries() {
        ChatEntry oldSelection = chatList.getSelectedValue();

        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        ArrayList<ChatEntry> list = new ArrayList<ChatEntry>();
        for (int i = 0; i < chatEntities.size(); i++) {
            list.add((ChatEntry) chatEntities.get(i));
        }
        Collections.sort(list, Collections.reverseOrder());
        chatEntities.removeAllElements();
        for (ChatEntry c : list) {
            chatEntities.addElement(c);
        }

        chatList.setSelectedValue(oldSelection, false);
    }

    class ChatEntry implements Comparable<ChatEntry> {
        Conversation conversation;
        boolean unread;

        public ChatEntry(Conversation conversation, boolean unread) {
            this.conversation = conversation;
            this.unread = unread;
        }

        public Conversation getConversation() {
            return conversation;
        }

        public boolean getUnread() {
            return unread;
        }

        public void setUnread(boolean unread) {
            this.unread = unread;
        }

        @Override
        public int compareTo(ChatEntry o) {
            return conversation.getMessages().get(conversation.getMessages().size() - 1).getTimestamp().compareTo(
                    o.conversation.getMessages().get(o.conversation.getMessages().size() - 1).getTimestamp());
        }
    }

    class ChatListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            ChatEntry chatEntry = (ChatEntry) value;
            Conversation conversation = chatEntry.getConversation();
            String name = conversation.getConversationName();
            String participants = conversation.getParticipantsString();
            String lastMsg = "";
            if (conversation.getMessages().size() > 0) {
                lastMsg = conversation.getMessages().get(conversation.getMessages().size() - 1).getContent();
            }

            setText("<html><b>" + name + ": " + participants + "</b><br>&nbsp;&nbsp;" + lastMsg);

            if (isSelected) {
                chatEntry.setUnread(false);
                setForeground(Color.decode("#ffffff"));
                setBackground(Color.decode("#149dff"));
            } else {
                setBackground(Color.decode("#dedede"));
            }

            if (chatEntry.getUnread()) {
                setForeground(Color.decode("#1466ff"));
            }

            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            return this;
        }
    }

    private DefaultListModel<MsgEntry> getMessageEntities() {
        ListModel<MsgEntry> msgEntities = msgList.getModel();
        if (!(msgEntities instanceof DefaultListModel)) {
            DefaultListModel<MsgEntry> defaultMsgEntries = new DefaultListModel<MsgEntry>();
            msgList.setModel(defaultMsgEntries);
            return defaultMsgEntries;
        }
        return (DefaultListModel<MsgEntry>) msgEntities;
    }

    public void clearMsgList() {
        msgList.setModel(new DefaultListModel<MsgEntry>());
    }

    public void setMsgList(ArrayList<Message> messages) {
        DefaultListModel<MsgEntry> msgEntities = getMessageEntities();
        for (Message msg : messages) {
            boolean mine = client.getUsername().equals(msg.getSender());
            msgEntities.addElement(new MsgEntry(msg, mine, false));
        }
    }

    public void addNewMsgEntry(Message msg) {
        DefaultListModel<MsgEntry> msgEntities = getMessageEntities();
        boolean mine = client.getUsername().equals(msg.getSender());
        MsgEntry newMsg = new MsgEntry(msg, mine, false);
        msgEntities.addElement(newMsg);
    }

    public void updateMsgEntry(Message msg) {
        DefaultListModel<MsgEntry> msgEntities = getMessageEntities();
        for (int i = 0; i < msgEntities.size(); i++) {
            MsgEntry msgEntry = msgEntities.getElementAt(i);
            if (msgEntry.getMessage().getId() == msg.getId()) {
                boolean mine = client.getUsername().equals(msg.getSender());
                MsgEntry newMsg = new MsgEntry(msg, mine, true);
                msgEntities.setElementAt(newMsg, i);
                break;
            }
        }
    }

    public void removeMsgEntry(Message msg) {
        DefaultListModel<MsgEntry> msgEntities = getMessageEntities();
        for (int i = 0; i < msgEntities.size(); i++) {
            MsgEntry msgEntry = msgEntities.getElementAt(i);
            if (msgEntry.getMessage().getId() == msg.getId()) {
                msgEntities.remove(i);
                break;
            }
        }
    }

    class MsgEntry extends JPanel implements Comparable<MsgEntry> {
        private static final String STYLE_SHEET = "<style>" + ".msg-box { margin: 2px; }"
                + ".msg-box p { display: block; justify-items: end; }"
                + ".other-msg { color: #000000; background-color: #dedede; text-align: left; padding: 7px; margin-top: 2px; margin-bottom: 1px; }"
                + ".my-msg { color: #ffffff; background-color: #149dff; text-align: left; padding: 7px; margin-top: 2px; margin-bottom: 1px; }"
                + "</style>";

        Message message;
        boolean mine;
        boolean edited;

        public MsgEntry(Message message, boolean mine, boolean edited) {
            this.message = message;
            this.mine = mine;
            this.edited = edited;
        }

        public Message getMessage() {
            return message;
        }

        public boolean getMine() {
            return mine;
        }

        @Override
        public int compareTo(MsgEntry o) {
            return message.getTimestamp().compareTo(o.message.getTimestamp());
        }

        public String getFormattedHTML() {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/dd/yy, hh:mm a");
            String timestamp = message.getTimestamp().format(f);
            String sender = message.getSender();
            String content = message.getContent();
            String editTag = edited ? " (Edited)" : "";
            String style = mine ? "my-msg" : "other-msg";

            return "<html>" + STYLE_SHEET + "<div style='width: 275px' class=msg-box><p class=" + style + "><b>"
                    + sender + "</b>" + " - " + "<i>" + timestamp + editTag + "</i>" + "<br>" + content + "</p></div>";
        }
    }

    class MsgListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            MsgEntry msgEntry = (MsgEntry) value;
            setText(msgEntry.getFormattedHTML());
            setBackground(Color.decode(msgEntry.getMine() ? "#149dff" : "#dedede"));

            String selectedAction = (String) messageActions.getSelectedItem();

            if (isSelected) {
                if (!selectedAction.equals(SEND_ACTION)) {
                    currentMsg = msgEntry;

                    composeMessage.requestFocusInWindow();
                    composeMessage.setText("");

                    if (msgEntry.getMine()) {
                        if (selectedAction.equals(EDIT_ACTION)) {
                            composeMessage.setText(msgEntry.getMessage().getContent());
                            setBackground(Color.decode("#35A437"));
                        } else if (selectedAction.equals(DELETE_ACTION)) {
                            composeMessage.setText("Press ENTER to confirm delete...");
                            setBackground(Color.decode("#FF3E31"));
                        }
                    }
                } else {
                    currentMsg = null;
                }
            }

            return this;
        }
    }

    public void showMsgList() {
        // In "using chat" mode...
        participantsField.setText(currentChat.getConversation().getParticipantsString());
        participantsField.setEditable(false);
        clearMsgList();

        setMsgList(currentChat.getConversation().getMessages());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JScrollBar scrollBar = msgListScrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            }
        });
    }

    public void hideMsgList() {
        // In "creating chat" mode...
        currentChat = null;
        chatList.setSelectedValue(null, false);
        participantsField.setText("");
        participantsField.setEditable(true);
        participantsField.requestFocusInWindow();
        messageActions.setSelectedItem(SEND_ACTION);
        clearMsgList();
    }
}
