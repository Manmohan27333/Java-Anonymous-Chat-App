package client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private JFrame frame = new JFrame("JavaChat");
    private JTextArea chatArea = new JTextArea();
    private JTextField messageField = new JTextField();
    private JButton sendButton = new JButton("Send");
    private JList<String> userList = new JList<>(new DefaultListModel<>());
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Socket socket;

    public ChatClient() {
        setupUI();
    }

    private void setupUI() {
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JPanel bottom = new JPanel(new BorderLayout(5,5));
        bottom.add(messageField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Online"), BorderLayout.NORTH);
        right.add(new JScrollPane(userList), BorderLayout.CENTER);
        right.setPreferredSize(new Dimension(150, 0));

        frame.setLayout(new BorderLayout(5,5));
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);
        frame.add(right, BorderLayout.EAST);

        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // actions
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;
        out.println(text);
        messageField.setText("");
    }

    private void connect(String host, int port, String username) throws IOException {
    socket = new Socket(host, port);
    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

    out.println("USERNAME:" + username);

    // check first response before starting reader thread
    socket.setSoTimeout(500);
    try {
        String firstResponse = in.readLine();
        if (firstResponse != null && firstResponse.startsWith("ERROR:")) {
            throw new IOException(firstResponse.substring(6)); // remove "ERROR:"
        } else if (firstResponse != null) {
            // If first line is not error, process it normally in thread
            processServerLine(firstResponse);
        }
    } catch (SocketTimeoutException ignored) {
        // no immediate error, proceed
    }
    socket.setSoTimeout(0);

    new Thread(this::readLoop).start();
}


    private void readLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                final String s = line;
                SwingUtilities.invokeLater(() -> processServerLine(s));
            }
        } catch (IOException e) {
            e.printStackTrace();
            appendToChat("Disconnected from server.");
        }
    }

    private void processServerLine(String line) {
        if (line.startsWith("MSG:")) {
            String payload = line.substring(4);
            int idx = payload.indexOf(':');
            if (idx != -1) {
                String sender = payload.substring(0, idx);
                String msg = payload.substring(idx + 1);
                appendToChat(sender + ": " + msg);
            } else {
                appendToChat(payload);
            }
        } else if (line.startsWith("PM:")) {
            String payload = line.substring(3);
            int idx = payload.indexOf(':');
            if (idx != -1) {
                String sender = payload.substring(0, idx);
                String msg = payload.substring(idx + 1);
                appendToChat("[PM] " + sender + ": " + msg);
            } else {
                appendToChat("[PM] " + payload);
            }
        } else if (line.startsWith("SERVER:")) {
            appendToChat("[Server] " + line.substring("SERVER:".length()));
        } else if (line.startsWith("USERLIST:")) {
            String list = line.substring("USERLIST:".length());
            updateUserList(list);
        } else {
            appendToChat(line);
        }
    }


    private void appendToChat(String s) {
        chatArea.append(s + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void updateUserList(String csv) {
        DefaultListModel<String> model = (DefaultListModel<String>) userList.getModel();
        model.clear();
        if (!csv.trim().isEmpty()) {
            String[] parts = csv.split(",");
            for (String p : parts) model.addElement(p);
        }
    }

    private void start() {
        // show login dialog
        JPanel p = new JPanel(new GridLayout(3,2,5,5));
        JTextField tfUser = new JTextField();
        JTextField tfHost = new JTextField("127.0.0.1");
        JTextField tfPort = new JTextField("5000");
        p.add(new JLabel("Username:"));
        p.add(tfUser);
        p.add(new JLabel("Server IP:"));
        p.add(tfHost);
        p.add(new JLabel("Port:"));
        p.add(tfPort);

        int rc = JOptionPane.showConfirmDialog(null, p, "Connect to chat server", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (rc != JOptionPane.OK_OPTION) {
            System.exit(0);
        }

        username = tfUser.getText().trim();
        String host = tfHost.getText().trim();
        int port = Integer.parseInt(tfPort.getText().trim());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a username.");
            start();
            return;
        }

        try {
            connect(host, port, username);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Connection failed: " + e.getMessage());
            start();
            return;
        }

        frame.setTitle("JavaChat - " + username);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClient client = new ChatClient();
            client.start();
        });
    }
}
