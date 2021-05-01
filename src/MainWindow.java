package src;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.StringTokenizer;
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
    private final String SEND_ACTION = "SEND";
    private final String EDIT_ACTION = "EDIT";
    private final String DELETE_ACTION = "DELETE";

    private ChatEntry currentChat;
    private MsgEntry currentMsg;

    public MainWindow(Client client) {
        super("Chat");

        this.client = client;

        initializeComponents();

        setSize(650, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                composeMessage.setText("");
                messageActions.setSelectedItem(SEND_ACTION);
                participantsField.requestFocusInWindow();
            }
        });

        leaveChatButton = new JButton("Leave Chat");
        leaveChatButton.setForeground(Color.decode("#FF3E31"));
        leaveChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Leave chat!
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
                // Open account edit window!
            }
        });

        signOutButton = new JButton("Sign Out");
        signOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // call client quit!
                dispose();
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
        chatPanel.add(msgListScrollPane, BorderLayout.CENTER);

        composeBar = new JPanel();
        composeBar.setLayout(new BorderLayout());
        composeMessage = new JTextArea();
        composeMessage.setLineWrap(true);
        composeMessage.setWrapStyleWord(true);
        composeScrollPane = new JScrollPane(composeMessage);
        composeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        composeBar.add(composeScrollPane, BorderLayout.CENTER);

        composeMessage.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLineCount();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLineCount();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLineCount();
            }

            private void updateLineCount() {
                int lineCount = getWrappedLines(composeMessage);
                if (lineCount <= 3) {
                    composeMessage.setRows(lineCount);
                    composeBar.revalidate();
                    chatPanel.revalidate();
                }
            }
        });

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
        chatPanel.add(composeBar, BorderLayout.SOUTH);

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
                break;
            default:
                System.out.println("ERROR: invalid action attempted!"); // should never happen!
        }
    }

    public static int getWrappedLines(JTextComponent c) {
        int len = c.getDocument().getLength();
        int offset = 0;
        // Increase 10% for extra newlines
        StringBuffer buf = new StringBuffer((int) (len * 1.10));
        try {
            while (offset < len) {
                int end = javax.swing.text.Utilities.getRowEnd(c, offset);
                if (end < 0) {
                    break;
                }
                // Include the last character on the line
                end = Math.min(end + 1, len);
                String s = c.getDocument().getText(offset, end - offset);
                buf.append(s);
                // Add a newline if s does not have one
                if (!s.endsWith("\n")) {
                    buf.append('\n');
                }
                offset = end;
            }
        } catch (BadLocationException e) {
        }
        StringTokenizer token = new StringTokenizer(buf.toString(), "\n");
        int linesOfText = token.countTokens();
        if (linesOfText == 0)
            linesOfText = 1;
        return linesOfText;
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

            // UPDATE string parsing to proper conversation parsing
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
        private static final String STYLE_SHEET = ".chat-box { margin: 2px; }"
                + ".chat-box p { display: block; justify-items: end; }"
                + ".chat-msg1 { color: #000000; background-color: #dedede; text-align: left; padding: 7px; margin-top: 2px; margin-bottom: 2px; }"
                + ".chat-msg2 { color: #ffffff; background-color: #149dff; text-align: left; padding: 7px; margin-top: 2px; margin-bottom: 2px; }";

        private static final String HTML_FORMAT = "<style>" + STYLE_SHEET + "</style>";

        Message message;
        boolean mine;

        public MsgEntry(Message message, boolean mine, boolean edited) {
            this.message = message;
            this.mine = mine;
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

            return "<html>" + HTML_FORMAT + "<div style='width: 300px' class=chat-box><p class=chat-msg1><b>" + sender
                    + "</b>" + " - " + "<i>" + timestamp + "</i>" + "<br>" + content + "</p></div>";
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
                currentMsg = msgEntry;
                if (selectedAction.equals(EDIT_ACTION)) {
                    if (msgEntry.getMine()) {
                        composeMessage.setText(msgEntry.getMessage().getContent());
                        setBackground(Color.decode("#35A437"));
                    } else {
                        composeMessage.setText("");
                    }
                }
                if (selectedAction.equals(DELETE_ACTION)) {
                    composeMessage.setText("");

                    if (msgEntry.getMine()) {
                        setBackground(Color.decode("#FF3E31"));
                    }
                }
            }

            return this;
        }
    }

    public void showMsgList() {
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
        currentChat = null;
        chatList.setSelectedValue(null, false);
        participantsField.setText("");
        participantsField.setEditable(true);
        clearMsgList();
    }
}
