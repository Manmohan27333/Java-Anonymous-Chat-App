# Java-Anonymous-Chat-App
This project allow the user to chat with the others (Who are in the server) without logging in (Anonymously) and they can also chat privately to anyone(by /pm command).

# Java Chat Application

A simple multi-user chat application built in Java using **Sockets** for networking and **Swing** for the graphical client interface.

## Features

* **Server**:

  * Accepts multiple clients simultaneously.
  * Manages active users and broadcasts messages.
  * Provides user list updates in real-time.
  * Supports private messaging with `/pm <username> <message>`.

* **Client**:

  * User-friendly Swing GUI.
  * Real-time chat updates.
  * Displays list of online users.
  * Supports both public and private messages.

## Project Structure

```
├── client/
│   └── ChatClient.java      # GUI-based chat client
└── server/
    ├── ChatServer.java      # Main server class
    └── ClientHandler.java   # Handles each client connection
```

## How to Run

### 1. Start the Server

```bash
cd server
javac ChatServer.java ClientHandler.java
java server.ChatServer 5000
```

(Default port is **5000** if not provided.)

### 2. Start a Client

```bash
cd client
javac ChatClient.java
java client.ChatClient
```

* Enter **Username**, **Server IP**, and **Port** (default: `127.0.0.1`, `5000`).

### 3. Chatting

* Type a message and press **Enter** or click **Send** to broadcast.
* Send private messages with:

  ```
  /pm <username> <message>
  ```

## Example

* User A: `Hello everyone!`
* User B (private to A): `/pm A Hey, let’s talk privately!`

## Requirements

* **Java 8 or higher**
* Works on Windows, macOS, and Linux
