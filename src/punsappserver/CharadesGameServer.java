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
    private static final long CLEAR_COOLDOWN = 1000;
    private static int COUNTDOWN_SECONDS = 60;
    private static boolean countdownRunning = false;
    private static final Random random = new Random();

    private static final List<Socket> clientSockets = new CopyOnWriteArrayList<>();
    private static final Map<String, Socket> userSocketMap = new ConcurrentHashMap<>();
    private static long lastClearTime = 0;
    private static int drawingPlayerIndex = 0;
    private static List<String> words = new ArrayList<>();

    private static String randomWord;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Charades Game Server is running on port " + PORT);

            // Load words from a file into the 'words' list
            loadWordsFromFile();

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

    private static void loadWordsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("words_charades.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] wordsInLine = line.split(",\\s*"); // Split words by commas and optional spaces
                words.addAll(Arrays.asList(wordsInLine));
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

    public void onColorReceived(String messageServer) {
        Gson gson = new Gson();
        Message colorMessage = gson.fromJson(messageServer, Message.class);

        String username = colorMessage.getUsername();
        String color = colorMessage.getColor();

        Socket senderSocket = CharadesGameServer.getSocketForUser(username);

        // Broadcast the color change to other clients
        broadcastColorChange(color, senderSocket);

        // Set the color for the sender client
        setClientColor(color, senderSocket);
    }

    private void broadcastColorChange(String color, Socket senderSocket) {
        for (Socket socket : clientSockets) {
            if (!socket.equals(senderSocket)) {
                try {
                    PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);

                    Message colorMessage = new Message();
                    colorMessage.setUsername("Server");
                    colorMessage.setMessageType("COLOR_CHANGE");
                    colorMessage.setColor(color);

                    String json = new Gson().toJson(colorMessage);
                    socketOut.println(json);

                    System.out.println("Sent color change to " + getUsernameForSocket(socket) + ": " + color);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

            Socket currentDrawingSocket = clientSockets.get(drawingPlayerIndex);
            randomWord = getRandomWord(); // Initialize random word
            String username = getUsernameForSocket(currentDrawingSocket);
            Message message = new Message();
            message.setMessageType("CHAT");
            message.setChat("Turn to draw: " + username);
            String json = new Gson().toJson(message);
            broadcast(json);
            notifyDrawingPlayer(currentDrawingSocket, randomWord);

            while (COUNTDOWN_SECONDS >= 0) {
                COUNTDOWN_SECONDS--;
                if (COUNTDOWN_SECONDS < 0) {
                    COUNTDOWN_SECONDS = 60; // Reset countdown to 1 minute
                    changeDrawingPlayer();
                }
                broadcastCountdown(COUNTDOWN_SECONDS);
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
        message.setUsername("Server");
        message.setX(countdown);

        Gson gson = new Gson();
        String countdownMessage = gson.toJson(message, Message.class);

        broadcast(countdownMessage);
    }

    public static void addUser(String username, Socket socket) {
        userSocketMap.put(username, socket);
    }

    public static void removeUser(String username) {
        userSocketMap.remove(username);
    }

    public static Socket getSocketForUser(String username) {
        return userSocketMap.get(username);
    }

    public static void clearCanvas() {
        Message message = new Message();
        message.setMessageType("CLEAR_CANVAS");
        String json = new Gson().toJson(message);
        onClearCanvasReceived1(json);
    }

    private static void changeDrawingPlayer() {
        drawingPlayerIndex++;
        if (drawingPlayerIndex >= clientSockets.size()) {
            drawingPlayerIndex = 0; // Reset to the first client socket if reached the end
        }

        // Notify the current drawing player
        Socket currentDrawingSocket = clientSockets.get(drawingPlayerIndex);

        // Ensure a new random word that is different from the previous one
        String newRandomWord;
        do {
            newRandomWord = getRandomWord();
        } while (newRandomWord.equals(randomWord));

        randomWord = newRandomWord;

        notifyDrawingPlayer(currentDrawingSocket, randomWord);
        clearCanvas();
    }

    private static void notifyDrawingPlayer(Socket drawingSocket, String word) {
        String username = getUsernameForSocket(drawingSocket);

        Message message = new Message();
        message.setMessageType("CHAT");
        message.setUsername("Server");
        message.setChat("Turn to draw: " + username);
        String json = new Gson().toJson(message);
        broadcast(json);

        message = new Message();
        message.setMessageType("CHAT");
        message.setUsername("Server");
        message.setChat("Your word is: " + word);
        json = new Gson().toJson(message);
        sendToClient(json, drawingSocket);

        message = new Message();
        message.setMessageType("PERMISSION");
        message.setUsername("Server");
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

    private static void setClientColor(String color, Socket clientSocket) {
        try {
            PrintWriter socketOut = new PrintWriter(clientSocket.getOutputStream(), true);

            Message colorMessage = new Message();
            colorMessage.setUsername("Server");
            colorMessage.setMessageType("COLOR_CHANGE");
            colorMessage.setColor(color);

            String json = new Gson().toJson(colorMessage);
            socketOut.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRandomWord() {
        return words.get(random.nextInt(words.size()));
    }

    @Override
    public void onChatMessageReceived(String username, String chatMessage) {
        String trimmedChatMessage = chatMessage.trim().toLowerCase();

        if (trimmedChatMessage.equals(randomWord.toLowerCase())) {
            // Handle guessed word message
            Message guessedWordMessage = new Message();
            guessedWordMessage.setMessageType("CHAT");
            guessedWordMessage.setUsername("Server");
            guessedWordMessage.setChat(username + " guessed the word! - " + randomWord);

            String winMessage = new Gson().toJson(guessedWordMessage);
            broadcast(winMessage);

            // Reset countdown
            COUNTDOWN_SECONDS = 60;

            // Change drawing player
            changeDrawingPlayer();
        } else {
            // Handle regular chat messages
            Message regularChatMessage = new Message();
            regularChatMessage.setMessageType("CHAT");
            regularChatMessage.setUsername(username);
            regularChatMessage.setChat(chatMessage);
            String regularChatJson = new Gson().toJson(regularChatMessage);
            broadcast(regularChatJson);
        }
    }
}
