package punsappserver;

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class CharadesGameServer implements ServerListener {
    private static final int PORT = 3000;
    static List<Socket> clientSockets = new ArrayList<>();
    private static long lastClearTime = 0;
    private static final long CLEAR_COOLDOWN = 1000;
    private static int countdownSeconds = 60;
    private static boolean countdownRunning = false;
    static Map<String, Socket> userSocketMap = new HashMap<>();
    private static int drawingPlayerIndex = 0;


    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Charades Game Server is running on port " + PORT);

            //startCountdownTimer();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Adding the client to the list of clients
                clientSockets.add(clientSocket);

                // Creating a thread to handle the client
                ClientHandler clientHandler = new ClientHandler(clientSocket, new CharadesGameServer());
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

                broadcastPlayerCount();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(String message) {
        // Broadcast chat message to all clients
        broadcast(message);
    }

    public void onClearCanvasReceived(String message) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClearTime > CLEAR_COOLDOWN) {
            broadcast(message);
            lastClearTime = currentTime;
        }
    }

    private static void broadcast(String message) {
        for (Socket socket : clientSockets) {
            try {
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                socketOut.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastPlayerCount() {
        Message message = new Message();
        message.setMessageType("PLAYER_COUNT");
        message.setX(clientSockets.size());

        Gson gson = new Gson();
        String playerCountMessage = gson.toJson(message, Message.class);

        broadcast(playerCountMessage);
    }

    public static void onCountdownStartReceived() {
        countdownRunning = true;
        startCountdownTimer();
    }

    private static void startCountdownTimer() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            changeDrawingPlayer();
            while (countdownSeconds >= 0) {
                countdownSeconds--;
                if (countdownSeconds < 0) {
                    countdownSeconds = 60; // Reset countdown to 1 minute
                    changeDrawingPlayer();
                }
                broadcastCountdown(countdownSeconds);
                //changeDrawingPlayer();
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void broadcastCountdown(int countdown) {
        Message message = new Message();
        message.setMessageType("COUNTDOWN");
        message.setX(countdown);

        Gson gson = new Gson();
        String countdownMessage = gson.toJson(message, Message.class);

        broadcast(countdownMessage);
    }

    public static void addUser(String username, Socket socket) {
        userSocketMap.put(username, socket);
    }

    // Method to remove a user from the map
    public static void removeUser(String username) {
        userSocketMap.remove(username);
    }

    // Method to get the socket for a given username
    public static Socket getSocketForUser(String username) {
        return userSocketMap.get(username);
    }

    private static void changeDrawingPlayer() {
        drawingPlayerIndex++;
        if (drawingPlayerIndex >= clientSockets.size()) {
            drawingPlayerIndex = 0; // Reset to the first client socket if reached the end
        }
        // Notify the current drawing player
        Socket currentDrawingSocket = clientSockets.get(drawingPlayerIndex);
        notifyDrawingPlayer(currentDrawingSocket);
    }

    private static void notifyDrawingPlayer(Socket drawingSocket) {
        String username = getUsernameForSocket(drawingSocket);
        Message message = new Message();
        message.setMessageType("CHAT");
        message.setChat("Turn to draw: " + username);
        String json = new Gson().toJson(message);
        broadcast(json);

        message = new Message();
        message.setMessageType("PERMISSION");
        message.setChat(username);
        json = new Gson().toJson(message);
        broadcast(json);
    }

    private static String getUsernameForSocket(Socket socket) {
        for (Map.Entry<String, Socket> entry : userSocketMap.entrySet()) {
            if (entry.getValue().equals(socket)) {
                return entry.getKey(); // Return the username for the given socket
            }
        }
        return null; // Return null if the socket is not found in the map
    }
}
