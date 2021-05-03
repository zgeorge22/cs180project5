import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicArrowButton;

import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This is the main GUI of the chat application where the user is able to chat
 * with other users.
 *
 * <p>
 * Purdue University -- CS18000 -- Spring 2021 -- Project 5
 * </p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack
 *         Dorkin
 * @version May 3rd, 2021
 */

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
    private static final String SEND_ACTION = "SEND";
    private static final String EDIT_ACTION = "EDIT";
    private static final String DELETE_ACTION = "DELETE";

    private ChatEntry currentChat;
    private MsgEntry currentMsg;

    public MainWindow(Client client) {
        super("Chat Window - " + client.getUsername());

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

    // Sets up all GUI compoentns in the desired layout. Initializes any event
    // listeners for buttons or key strokes.
    protected void initializeComponents() {
        content = getContentPane();
        content.setLayout(new BorderLayout());

        // ====================== SIDE PANEL LAYOUT ======================

        // Create chat button
        createChatButton = new JButton("Create Chat");
        createChatButton.setForeground(Color.decode("#35A437"));
        createChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideMsgList();
            }
        });

        // Leave chat button
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

        // Chat list
        chatList = new JList<>();
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

        // Import chat button
        importChatButton = new JButton("Import Chat(s)");
        importChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Importing conversations is not " + "supported right now.",
                        "Warning", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Export chat button
        exportChatButton = new JButton("Export Chat(s)");
        exportChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentChat != null) {
                    Conversation convo = currentChat.getConversation();
                    convo.exportToCSV();
                    String exportName = convo.getConversationName() + convo.getConversationId() + ".csv";

                    JOptionPane.showMessageDialog(null, "Exported conversation to " + exportName, "Warning",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "No chat selected to export!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Account button (change password)
        accountButton = new JButton("Account");
        accountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = JOptionPane.showInputDialog("Enter new password: ");

                if (Client.validPassword(password)) {
                    client.requestEditPassword(password);
                }
            }
        });

        // Sign out button
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

        // ====================== CHAT PANEL LAYOUT ======================

        // Conversation participants list
        participantsField = new JTextField();

        convoHeader = new JPanel();
        convoHeader.setLayout(new BorderLayout());
        convoHeader.add(new JLabel(" Participants: "), BorderLayout.WEST);
        convoHeader.add(participantsField, BorderLayout.CENTER);

        // Message list display for current chat
        msgList = new JList<MsgEntry>();
        msgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        msgList.setCellRenderer(new MsgListCellRenderer());

        msgListScrollPane = new JScrollPane(msgList);
        msgListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Compose message
        composeMessage = new JTextArea();
        composeMessage.setLineWrap(true);
        composeMessage.setWrapStyleWord(true);
        composeScrollPane = new JScrollPane(composeMessage);
        composeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Check for ENTER button (submit action attempt)
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

        // Message action mode dropdown
        messageActions = new JComboBox<>();
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

        // Execute button (submit action attempt)
        BasicArrowButton execute = new BasicArrowButton(BasicArrowButton.EAST);
        execute.setPreferredSize(new Dimension(40, 40));
        execute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptMessageAction();
            }
        });

        composeBar = new JPanel();
        composeBar.setLayout(new BorderLayout());
        composeBar.add(messageActions, BorderLayout.WEST);
        composeBar.add(composeScrollPane, BorderLayout.CENTER);
        composeBar.add(execute, BorderLayout.EAST);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, msgListScrollPane, composeBar);
        splitPane.setResizeWeight(1);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        chatPanel.add(convoHeader, BorderLayout.NORTH);
        chatPanel.add(splitPane, BorderLayout.CENTER);

        content.add(sidePanel, BorderLayout.WEST);
        content.add(chatPanel, BorderLayout.CENTER);
    }

    // Called by pressing ENTER or the execute arrow button
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
                        JOptionPane.showMessageDialog(null, "You can only edit messages" + " that you have created!",
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
                        JOptionPane.showMessageDialog(null, "You can only delete messages "
                                                      + "that you have created!",
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

    // Get the list model used to update the GUI chat list
    private DefaultListModel<ChatEntry> getChatEntities() {
        ListModel<ChatEntry> chatEntities = chatList.getModel();
        if (!(chatEntities instanceof DefaultListModel)) {
            DefaultListModel<ChatEntry> defaultChatEntries = new DefaultListModel<>();
            chatList.setModel(defaultChatEntries);
            return defaultChatEntries;
        }
        return (DefaultListModel<ChatEntry>) chatEntities;
    }

    // Set (technically append) the chat list window with a list of conversations
    // Used initially after data dump
    public void setChatList(ArrayList<Conversation> conversations) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (Conversation convo : conversations) {
            chatEntities.addElement(new ChatEntry(convo, false));
        }
    }

    // Add a new conversation to the list of conversations
    // Used upon being added to a new conversation
    public void addNewChat(Conversation convo) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        ChatEntry newChat = new ChatEntry(convo, true);
        chatEntities.add(0, newChat);
    }

    // Update a chat list display item with new conversation data
    // Used when chat members change or when a message is received/edited/deleted
    public void updateChatEntry(Conversation convo) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (int i = 0; i < chatEntities.size(); i++) {
            ChatEntry chatEntry = chatEntities.getElementAt(i);
            if (chatEntry.getConversation().getConversationId() == convo.getConversationId()) {
                final int index = i;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        chatEntities.setElementAt(new ChatEntry(convo, true), index);
                        // Refresh the chat order (the latest message timestamp may have changed)
                        sortChatEntries();

                        // Refresh message list if the current chat was the one that was just updated
                        if (currentChat != null) {
                            if (currentChat.getConversation().getConversationId() == convo.getConversationId()) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        showMsgList();
                                    }
                                });
                            }
                        }
                    }
                });
                break;
            }
        }
    }

    // Remove a conversation from the chat list
    // Used when we receive a confirmation that we can leave the chat
    public void removeChatEntry(Conversation convo) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (int i = 0; i < chatEntities.size(); i++) {
            ChatEntry chatEntry = chatEntities.getElementAt(i);
            if (chatEntry.getConversation().getConversationId() == convo.getConversationId()) {
                final int index = i;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        chatEntities.remove(index);
                    }
                });
                break;
            }
        }
    }

    // Sort chat list items by timestamp (most recent at the top)
    public void sortChatEntries() {
        ChatEntry oldSelection = chatList.getSelectedValue();

        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        ArrayList<ChatEntry> list = new ArrayList<>();
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

    /**
     * Wrapper class for extra conversation information relevant to GUI
     *
     * <p>
     * Purdue University -- CS18000 -- Spring 2021 -- Project 5
     * </p>
     *
     * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack
     *         Dorkin
     * @version May 3rd, 2021
     */
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

        // Used to sort chats by timestamp
        @Override
        public int compareTo(ChatEntry o) {
            return conversation.getMessages().get(conversation.getMessages().size() - 1).getTimestamp().compareTo(
                    o.conversation.getMessages().get(o.conversation.getMessages().size() - 1).getTimestamp());
        }
    }

    /**
     * Renderer for a chat list item
     *
     * <p>
     * Purdue University -- CS18000 -- Spring 2021 -- Project 5
     * </p>
     *
     * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack
     *         Dorkin
     * @version May 3rd, 2021
     */
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

    // Get the list model used to update the GUI message list
    private DefaultListModel<MsgEntry> getMessageEntities() {
        ListModel<MsgEntry> msgEntities = msgList.getModel();
        if (!(msgEntities instanceof DefaultListModel)) {
            DefaultListModel<MsgEntry> defaultMsgEntries = new DefaultListModel<>();
            msgList.setModel(defaultMsgEntries);
            return defaultMsgEntries;
        }
        return (DefaultListModel<MsgEntry>) msgEntities;
    }

    // Clear message list in window
    // Used before displaying a new selected chat or the create chat window
    public void clearMsgList() {
        msgList.setModel(new DefaultListModel<>());
    }

    // Set (technically append) the message list window with a list of messages
    // Used when a chat is selected to be displayed
    public void setMsgList(ArrayList<Message> messages) {
        DefaultListModel<MsgEntry> msgEntities = getMessageEntities();
        for (Message msg : messages) {
            boolean mine = client.getUsername().equals(msg.getSender());
            msgEntities.addElement(new MsgEntry(msg, mine, false));
        }
    }

    // Add a new message to the list of messages
    // Used upon receiving a new message
    public void addNewMsgEntry(Message msg) {
        DefaultListModel<MsgEntry> msgEntities = getMessageEntities();
        boolean mine = client.getUsername().equals(msg.getSender());
        MsgEntry newMsg = new MsgEntry(msg, mine, false);
        msgEntities.addElement(newMsg);
    }

    // Update a message list display item with new message data
    // Used when a message is edited
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

    // Remove a message from the message list
    // Used when a message is deleted
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

    // Switches the chat window to the current chat selected by filling in messages
    // Used when a chat is selected in the chat list window
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

    // Blanks out the chat window
    // Used when ready to create chat
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

    /**
     * Wrapper class for extra message information relevant to GUI
     *
     * <p>
     * Purdue University -- CS18000 -- Spring 2021 -- Project 5
     * </p>
     *
     * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack
     *         Dorkin
     * @version May 3rd, 2021
     */
    class MsgEntry extends JPanel implements Comparable<MsgEntry> {
        private static final String STYLE_SHEET = "<style>" + ".msg-box { margin: 2px; }"
                + ".msg-box p { display: block; justify-items: end; }"
                + ".other-msg { color: #000000; background-color: #dedede; text-align: left; padding: 7px;"
                + " margin-top: 2px; margin-bottom: 1px; }"
                + ".my-msg { color: #ffffff; background-color: #149dff; text-align: left; padding: 7px;"
                + " margin-top: 2px; margin-bottom: 1px; }" + "</style>";

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
            String msgContent = message.getContent();
            String editTag = edited ? " (Edited)" : "";
            String style = mine ? "my-msg" : "other-msg";

            return "<html>" + STYLE_SHEET + "<div style='width: 275px' class=msg-box><p class=" + style + "><b>"
                    + sender + "</b>" + " - " + "<i>" + timestamp + editTag + "</i>" + "<br>" + msgContent
                    + "</p></div>";
        }
    }

    /**
     * Renderer for a message list item
     *
     * <p>
     * Purdue University -- CS18000 -- Spring 2021 -- Project 5
     * </p>
     *
     * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack
     *         Dorkin
     * @version May 3rd, 2021
     */
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
}
