package punsappserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

//Server for waiting room
public class CharadesGameServer {
    private static final int PORT = 3000;
    // Lists to hold client sockets, user-to-socket mappings, and player scores
    static final List<Socket> clientSockets = new CopyOnWriteArrayList<>();
    static final Map<String, Socket> userSocketMap = new ConcurrentHashMap<>();
    static final Map<Socket, Integer> playerScoresMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            // ServerSocket setup to listen for incoming connections on a specific port
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Charades Game Server is running on port " + PORT);

//            DatabaseConnection connectNow = new DatabaseConnection();
//            Connection connectDB = connectNow.getConnection();
//
//            String query1 = "SELECT * FROM CharadesLeaderboard";
//            //String test = "select count(*) from charadesleaderboard";
//
//            try {
//                Statement statement = connectDB.createStatement();
//                ResultSet queryResult = statement.executeQuery(query1);
//
//                while (queryResult.next()) {
//                    System.out.println(queryResult.getInt(1));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            while (true) {
                // Accept incoming client connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Adding the client socket to the list of clients
                clientSockets.add(clientSocket);

                // Creating a thread to handle the client connection
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

                // Broadcast the updated player count to all connected clients
                BroadcastManagement.broadcastPlayerCount();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to add a user with their associated socket to the server's mappings
    public static void addUser(String username, Socket socket) {
        userSocketMap.put(username, socket);
        playerScoresMap.put(socket, 0); // Initialize player score to 0 for the new user
    }

    // Method to remove a user from the server's mappings
    public static void removeUser(String username) {
        userSocketMap.remove(username);
    }
}
