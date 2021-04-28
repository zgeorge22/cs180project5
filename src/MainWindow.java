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
    private JButton createChatButton;
    private JList<ChatEntry> chatList;
    private JScrollPane chatListScrollPane;
    private JPanel botPanel;
    private JPanel chatPanel;
    private JTextPane convoDisplay;
    private JScrollPane convoDisplayScrollPane;
    private JPanel composeBar;
    private JScrollPane composeScrollPane;
    private JTextArea composeMessage;
    private JButton sendButton;

    private Conversation currentChat;

    private static final String STYLE_SHEET = ".chat-box { margin: 2px; }"
            + ".chat-box p { display: block; word-wrap: break-word; justify-items: end; }"
            + ".chat-msg1 { color: #000000; background-color: #dedede; text-align: left; padding: 7px; margin-top: 2px; margin-bottom: 2px; }"
            + ".chat-msg2 { color: #ffffff; background-color: #149dff; text-align: left; padding: 7px; margin-top: 2px; margin-bottom: 2px; }";

    private static final String HTML_FORMAT = "<style>" + STYLE_SHEET + "</style>"
            + "<div id=content class=chat-box></div>";

    private static final String OTHER_CHAT_FORMAT = "<p class=chat-msg1>%s</p>";
    private static final String MY_CHAT_FORMAT = "<p class=chat-msg2>%s</p>";

    public MainWindow(Client client) {
        super("Chat");

        this.client = client;

        initializeComponents();

        setSize(650, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    protected void initializeComponents() {
        content = getContentPane();
        content.setLayout(new BorderLayout());

        // SIDE PANEL LAYOUT
        sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout());

        createChatButton = new JButton("Create New Chat");
        createChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // UPDATE test button functionality
                // addNewChat("0Created chat");

                // JOptionPane.showMessageDialog(MainWindow.this, "Button Pressed", "Hey",
                // JOptionPane.INFORMATION_MESSAGE);

                System.out.println(convoDisplay.getText());
            }
        });

        sidePanel.add(createChatButton, BorderLayout.NORTH);

        chatList = new JList<ChatEntry>();
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setCellRenderer(new ChatListCellRenderer());
        chatList.setFixedCellWidth(250); // .setFixedCellHeight(50);
        chatList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    currentChat = chatList.getSelectedValue().getConversation();
                    if (currentChat != null) {
                        fillConvoDisplay();
                        clearComposeMessage();
                    }
                }
            }
        });

        chatListScrollPane = new JScrollPane(chatList);
        chatListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidePanel.add(chatListScrollPane, BorderLayout.CENTER);

        botPanel = new JPanel();
        botPanel.add(new JButton("Account"));
        botPanel.add(new JButton("Sign Out"));
        sidePanel.add(botPanel, BorderLayout.SOUTH);

        // CHAT PANEL LAYOUT
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        // chatPanel.setBackground(Color.gray);
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        convoDisplay = new JTextPane();
        convoDisplay.setContentType("text/html");
        convoDisplay.setEditable(false);
        convoDisplay.setText(HTML_FORMAT);

        convoDisplayScrollPane = new JScrollPane(convoDisplay);
        convoDisplayScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chatPanel.add(convoDisplayScrollPane, BorderLayout.CENTER); // chatPanel.remove(convoDisplayScrollPane);

        composeBar = new JPanel();
        composeBar.setLayout(new BorderLayout());
        composeMessage = new JTextArea();
        composeMessage.setLineWrap(true);
        composeMessage.setWrapStyleWord(true);
        composeScrollPane = new JScrollPane(composeMessage);
        composeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        composeBar.add(composeScrollPane, BorderLayout.CENTER); // ADD ACTION LISTENER TO TEXTFIELD

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

        sendButton = new JButton("Send");
        composeBar.add(sendButton, BorderLayout.EAST);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentChat != null && !composeMessage.getText().equals("")) {
                    client.sendMessage(currentChat, composeMessage.getText());
                }
            }
        });
        chatPanel.add(composeBar, BorderLayout.SOUTH);

        content.add(sidePanel, BorderLayout.WEST);
        content.add(chatPanel, BorderLayout.CENTER);
    }

    public void clearComposeMessage() {
        composeMessage.setText("");
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

    public void setChatList(ArrayList<Conversation> conversationList) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (Conversation convo : conversationList) {
            chatEntities.addElement(new ChatEntry(convo, false));
        }
    }

    public void addNewChat(Conversation convo) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        chatEntities.addElement(new ChatEntry(convo, true));
    }

    public void updateChatEntry(Conversation conversation) {
        DefaultListModel<ChatEntry> chatEntities = getChatEntities();
        for (int i = 0; i < chatEntities.size(); i++) {
            ChatEntry chatEntry = chatEntities.getElementAt(i);
            if (chatEntry.getConversation().getConversationId() == conversation.getConversationId()) {
                chatEntities.setElementAt(new ChatEntry(conversation, true), i);
                break;
            }
        }

        if (currentChat != null) {
            if (chatList.getSelectedValue().getConversation().getConversationId() == conversation.getConversationId()) {
                fillConvoDisplay();
            }
        }
    }

    public void sortChatEntries() {
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
            String participants = conversation.getParticipantsString();
            String lastMsg = conversation.getMessages().get(conversation.getMessages().size() - 1).getContent();

            setText("<html><b>" + participants + "</b><br>&nbsp;&nbsp;" + lastMsg);

            if (isSelected) {
                chatEntry.setUnread(false);
            }

            if (chatEntry.getUnread()) {
                setForeground(Color.BLUE);
            }

            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

            return this;
        }
    }

    // public void appendFriendMessage(String message) {
    // fillConvoDisplay(String.format(OTHER_CHAT_FORMAT, message));
    // }

    // public void appendMeMyMessage(String message) {
    // fillConvoDisplay(String.format(MY_CHAT_FORMAT, message));
    // }

    public void appendMessageToConvoDisplay(String message) {
        HTMLDocument document = (HTMLDocument) convoDisplay.getDocument();
        Element contentElement = document.getElement("content");
        try {
            if (contentElement.getElementCount() > 0) {
                Element lastElement = contentElement.getElement(contentElement.getElementCount() - 1);
                document = (HTMLDocument) contentElement.getDocument();
                document.insertAfterEnd(lastElement, message);
            } else {
                document.insertBeforeEnd(contentElement, message);
            }
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    private String formatHTMLMessage(Message message) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/dd/yy, hh:mm a");
        String timestamp = message.getTimestamp().format(f);
        String sender = message.getSender();
        String content = message.getContent();

        return "<b>" + sender + "</b>" + " - " + "<i>" + timestamp + "</i>" + "<br>" + content;
    }

    public void fillConvoDisplay() {
        convoDisplay.setText(HTML_FORMAT);
        for (Message m : currentChat.getMessages()) {
            // UPDATE LATER
            if (m.getSender().equals("Zach")) {
                appendMessageToConvoDisplay(String.format(MY_CHAT_FORMAT, formatHTMLMessage(m)));
            } else {
                appendMessageToConvoDisplay(String.format(OTHER_CHAT_FORMAT, formatHTMLMessage(m)));
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JScrollBar scrollBar = convoDisplayScrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            }
        });
    }
}
