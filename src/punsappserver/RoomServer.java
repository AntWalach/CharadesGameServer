package punsappserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//Server for game room
public class RoomServer implements Runnable {
    ServerSocket roomServerSocket;
    private final int roomPort;
    final List<Socket> clientSockets = new CopyOnWriteArrayList<>(); //List of clients sockets
    final Map<String, Socket> userSocketMap = new ConcurrentHashMap<>(); // Map of usernames and sockets of clients
    final Map<Socket, Integer> playerScoresMap = new ConcurrentHashMap<>(); // Map of clients sockets and scores
    static List<String> words = new ArrayList<>();

    protected int COUNTDOWN_SECONDS = 60; // Round time

    public RoomServer(int roomPort) throws IOException {
        this.roomPort = roomPort;
    }

    public void run() {
        try {
            // Initialize room server socket and start handling clients
            roomServerSocket = new ServerSocket(roomPort);
            System.out.println("Room server running on port " + roomPort);

            // Load words for the game
            WordListManagement.loadWordsFromFile();

            // Create game management instance for this room
            GameManagement gameManagement = new GameManagement(this);
            gameManagement.startCountdownTimer(roomPort % 10); // Start the countdown timer for the game

            // Accept incoming client connections
            while (true) {
                Socket clientSocket = roomServerSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Add the client to the list of clients
                clientSockets.add(clientSocket);

                // Create a thread to handle the client in this room
                RoomClientHandler roomClientHandler = new RoomClientHandler(clientSocket, this, gameManagement);
                Thread clientThread = new Thread(roomClientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send a message to a specific client socket
    static void sendToClient(String message, Socket socket) {
        try {
            PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketOut.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methods for managing users in the room
    public void addUser(String username, Socket socket) {
        userSocketMap.put(username, socket);
        playerScoresMap.put(socket, 0);
    }

    public void removeUser(String username) {
        userSocketMap.remove(username);
    }

    public Socket getSocketForUser(String username) {
        return userSocketMap.get(username);
    }

    // Method to get username based on the socket
    String getUsernameForSocket(Socket socket) {
        for (Map.Entry<String, Socket> entry : userSocketMap.entrySet()) {
            if (entry.getValue().equals(socket)) {
                return entry.getKey(); // Return the username for the given socket
            }
        }
        return null; // Return null if the socket is not found in the map
    }
}
