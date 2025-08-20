package server;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private int port;
    // thread-safe set of handlers
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Starting chat server on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket sock = serverSocket.accept();
                ClientHandler handler = new ClientHandler(sock, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast message to all clients
    public void broadcast(String message) {
        for (ClientHandler c : clients) {
            c.sendLine(message);
        }
    }

    public boolean isUsernameTaken(String name) {
        for (ClientHandler c : clients) {
            if (name.equalsIgnoreCase(c.getUsername())) {
                return true;
            }
        }
        return false;
    }

    public void addClient(ClientHandler c) {
        clients.add(c);
        updateUserList();
    }

    // Remove client (on disconnect)
    public void removeClient(ClientHandler c) {
        clients.remove(c);
        updateUserList();
    }

    // Create comma-separated user list and broadcast
    public void updateUserList() {
        StringBuilder sb = new StringBuilder();
        for (ClientHandler c : clients) {
            if (c.getUsername() != null) {
                if (sb.length() > 0) sb.append(",");
                sb.append(c.getUsername());
            }
        }
        //String userListMsg = "USERLIST:" + sb.toString();
        broadcast("USERLIST:" + sb);
    }
    public ClientHandler getClientByName(String name) {
        for (ClientHandler c : clients) {
            if (c.getUsername() != null && c.getUsername().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        int port = 5000;
        if (args.length >= 1) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
