package punsappserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final int PORT = 3000;
    static List<Socket> clientSockets = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Chat Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Dodawanie klienta do listy klientów
                clientSockets.add(clientSocket);

                // Tworzenie wątku obsługującego klienta
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastChatMessage(String message) {
        for (Socket socket : clientSockets) {
            try {
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                socketOut.println("CHAT " + message); // Prefix with "CHAT" for text messages
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void broadcastCoordinates(double x, double y) {
        String coordinates = x + " " + y; // Format coordinates as space-separated x y
        for (Socket socket : clientSockets) {
            try {
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                socketOut.println("COORDINATES " + coordinates); // Prefix with "COORDINATES" for drawing
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
