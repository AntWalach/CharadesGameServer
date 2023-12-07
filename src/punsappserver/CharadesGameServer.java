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
    private static List<String> words = new ArrayList<>();


    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Charades Game Server is running on port " + PORT);

            // Load words from a file into the 'words' list
            try {
                BufferedReader reader = new BufferedReader(new FileReader("words_charades.txt"));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] wordsInLine = line.split(",\\s*"); // Split words by commas and optional spaces
                    words.addAll(Arrays.asList(wordsInLine));
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

    public static void onClearCanvasReceived1(String message) {
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

    private static void sendToClient(String message, Socket socket) {
        try {
            PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketOut.println(message);
        } catch (IOException e) {
            e.printStackTrace();
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

            //changeDrawingPlayer();
            Socket currentDrawingSocket = clientSockets.get(drawingPlayerIndex);
            String randomWord = words.get(new Random().nextInt(words.size())-1);
            String username = getUsernameForSocket(currentDrawingSocket);
            Message message = new Message();
            message.setMessageType("CHAT");
            message.setChat("Turn to draw: " + username);
            String json = new Gson().toJson(message);
            broadcast(json);
            notifyDrawingPlayer(currentDrawingSocket, randomWord);

            while (countdownSeconds >= 0) {
                countdownSeconds--;
                if (countdownSeconds < 0) {
                    countdownSeconds = 60; // Reset countdown to 1 minute
                    drawingPlayerIndex++;
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

    public static void clearCanvas(){
        Message message = new Message();
        message.setMessageType("CLEAR_CANVAS");
        String json = new Gson().toJson(message);
        onClearCanvasReceived1(json);
    }

    private static void changeDrawingPlayer() {
        //drawingPlayerIndex++;
        if (drawingPlayerIndex >= clientSockets.size()) {
            drawingPlayerIndex = 0; // Reset to the first client socket if reached the end
        }

        String randomWord = words.get(new Random().nextInt(words.size())-1);

        // Notify the current drawing player
        Socket currentDrawingSocket = clientSockets.get(drawingPlayerIndex);
        notifyDrawingPlayer(currentDrawingSocket, randomWord);
        clearCanvas();
    }

    private static void notifyDrawingPlayer(Socket drawingSocket, String word) {
        String username = getUsernameForSocket(drawingSocket);
        Message message = new Message();
        message.setMessageType("CHAT");
        message.setChat("Turn to draw: " + username);
        String json = new Gson().toJson(message);
        broadcast(json);

        message = new Message();
        message.setMessageType("CHAT");
        message.setChat("Your word is: " + word);
        json = new Gson().toJson(message);
        sendToClient(json, drawingSocket);

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
