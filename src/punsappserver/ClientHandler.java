package punsappserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message: " + message);

                if (message.startsWith("COORDINATES")) {
                    // Extract x and y coordinates from the message
                    String[] parts = message.split(" ");
                    if (parts.length >= 3) {
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);

                        // Broadcast the coordinates to other clients
                        //serverListener.onCoordinatesReceived(x, y);
                        ChatServer.broadcastCoordinates(x,y);
                    }
                } else {
                    // For other message types, broadcast as usual
                    ChatServer.broadcastChatMessage(message);
                }

//                String[] parts = message.split(" ", 2);
//                if (parts.length >= 2) {
//                    String messageType = parts[0];
//                    String messageContent = parts[1];
//
//                    if (messageType.equals("CHAT")) {
//                        // This is a regular chat message, broadcast it to other clients for chat
//                        ChatServer.broadcastChatMessage(messageContent);
//                    } else if (messageType.equals("COORDINATES")) {
//                        // This is a drawing-related message, broadcast it to other clients for drawing
//                        ChatServer.broadcastCoordinates(messageContent);
//                    }
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Usuń klienta z listy klientów i zamknij połączenie
            ChatServer.clientSockets.remove(clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
