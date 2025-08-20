package server;
import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendLine(String line) {
        out.println(line);
    }

    @Override
    public void run() {
        try {
            String first = in.readLine();
            if (first != null && first.startsWith("USERNAME:")) {
                String desiredUsername = first.substring("USERNAME:".length()).trim();
                if (server.isUsernameTaken(desiredUsername)) {
                    out.println("ERROR:Username already taken");
                    close();
                    return;
                }
                username = desiredUsername;
                server.addClient(this);
                System.out.println(username + " connected from " + socket.getRemoteSocketAddress());
                server.broadcast("SERVER:" + username + " joined the chat");
            } else {
                close();
                return;
            }

            String line;
            while ((line = in.readLine()) != null) {
                if(line.startsWith("/pm")){
                    handlePrivateMessage(line);
                }
                else{
                    String msg = "MSG:" + username + ":" + line;
                    server.broadcast(msg);
                }
            }
        } catch (IOException e) {
            // ignore
        } finally {
            close();
        }
    }
    private void handlePrivateMessage(String line) {
        // format: /pm recipient message
        String[] parts = line.split(" ", 3);
        if (parts.length < 3) {
            sendLine("SERVER:Usage: /pm <username> <message>");
            return;
        }
        String recipientName = parts[1];
        String message = parts[2];

        ClientHandler recipient = server.getClientByName(recipientName);
        if (recipient == null) {
            sendLine("SERVER:User '" + recipientName + "' not found.");
            return;
        }

        // send to recipient
        recipient.sendLine("PM:" + username + ":" + message);
        // send to sender
        sendLine("PM:You to " + recipientName + ":" + message);
    }


    private void close() {
        try {
            if (username != null) {
                System.out.println(username + " disconnected.");
                server.broadcast("SERVER:" + username + " left the chat");
            }
            server.removeClient(this);
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

