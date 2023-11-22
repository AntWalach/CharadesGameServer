package punsappserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private ServerListener serverListener;

    public ClientHandler(Socket clientSocket, ServerListener serverListener) {
        this.clientSocket = clientSocket;
        this.serverListener = serverListener;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message: " + message);

                if (message.startsWith("COORDINATES")) {
                    handleCoordinatesMessage(message);
                } else if (message.equals("CLEAR_CANVAS")) {
                    serverListener.onClearCanvasReceived();
                    //broadcastClearCanvasCommand();
                    //break;
                } else {
                    handleChatMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeClientSocket();
        }
    }

    private void handleCoordinatesMessage(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 3) {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            serverListener.onCoordinatesReceived(x, y);
        }
    }

    private void handleChatMessage(String message) {
        serverListener.onChatMessageReceived(message);
    }

    private void broadcastClearCanvasCommand() {
        // Iterate through all connected clients and send clear command to each
        for (Socket clientSocket : ChatServer.clientSockets) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("CLEAR_CANVAS");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeClientSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}