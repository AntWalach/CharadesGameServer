package punsappserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CharadesGameServer implements ServerListener {
    private static final int PORT = 3000;
    static List<Socket> clientSockets = new ArrayList<>();
    private static long lastClearTime = 0;
    private static final long CLEAR_COOLDOWN = 1000;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Charades Game Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Adding the client to the list of clients
                clientSockets.add(clientSocket);

                // Creating a thread to handle the client
                ClientHandler clientHandler = new ClientHandler(clientSocket, new CharadesGameServer());
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChatMessageReceived(String message) {
        // Broadcast chat message to all clients
        broadcast("CHAT " + message);
    }

    @Override
    public void onCoordinatesReceived(double x, double y) {
        // Broadcast coordinates to all clients
        broadcast("COORDINATES " + x + " " + y);
    }

    public void onClearCanvasReceived() {
        // Broadcast clear canvas message to all clients
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClearTime > CLEAR_COOLDOWN) {
            broadcast("CLEAR_CANVAS");
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
}
