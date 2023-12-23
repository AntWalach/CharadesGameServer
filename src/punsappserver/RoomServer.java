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

public class RoomServer implements Runnable{
    ServerSocket roomServerSocket;
    private final int roomPort;
     final List<Socket> clientSockets = new CopyOnWriteArrayList<>();
    static final Map<String, Socket> userSocketMap = new ConcurrentHashMap<>();
    static final Map<Socket, Integer> playerScoresMap = new ConcurrentHashMap<>();
    static List<String> words = new ArrayList<>();
    protected static String randomWord;
    protected static boolean countdownRunning = false;

    protected  int COUNTDOWN_SECONDS = 60;



    public RoomServer(int roomPort) throws IOException {
        this.roomPort = roomPort;

    }

    public void run() {
        try {

            roomServerSocket = new ServerSocket(roomPort);
            System.out.println("Room server running on port " + roomPort);
            // Obsługa klientów na nowym porcie

            WordListManagement.loadWordsFromFile();

            GameManagement gameManagement = new GameManagement(this);
            gameManagement.startCountdownTimer(roomPort%10);

            while (true) {
                Socket clientSocket = roomServerSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Adding the client to the list of clients
                clientSockets.add(clientSocket);

                // Creating a thread to handle the client
                RoomClientHandler roomClientHandler = new RoomClientHandler(clientSocket, this,gameManagement);
                Thread clientThread = new Thread(roomClientHandler);
                clientThread.start();
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
