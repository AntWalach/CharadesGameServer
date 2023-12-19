package punsappserver;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class CharadesGameServer implements ServerListener {
    private static final int PORT = 3000;
    protected static boolean countdownRunning = false;

    static final List<Socket> clientSockets = new CopyOnWriteArrayList<>();
    static final Map<String, Socket> userSocketMap = new ConcurrentHashMap<>();
    static final Map<Socket, Integer> playerScoresMap = new ConcurrentHashMap<>();
    static List<String> words = new ArrayList<>();

    protected static String randomWord;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Charades Game Server is running on port " + PORT);

            // Load words from a file into the 'words' list
            WordListManagement.loadWordsFromFile();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Adding the client to the list of clients
                clientSockets.add(clientSocket);

                // Creating a thread to handle the client
                ClientHandler clientHandler = new ClientHandler(clientSocket, new CharadesGameServer());
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

                BroadcastManagement.broadcastPlayerCount();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void sendToClient(String message, Socket socket) {
        try {
            PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketOut.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(String username, Socket socket) {
        userSocketMap.put(username, socket);
        playerScoresMap.put(socket, 0);
    }

    public static void removeUser(String username) {
        userSocketMap.remove(username);
    }

    public static Socket getSocketForUser(String username) {
        return userSocketMap.get(username);
    }

    static String getUsernameForSocket(Socket socket) {
        for (Map.Entry<String, Socket> entry : userSocketMap.entrySet()) {
            if (entry.getValue().equals(socket)) {
                return entry.getKey(); // Return the username for the given socket
            }
        }
        return null; // Return null if the socket is not found in the map
    }
}
