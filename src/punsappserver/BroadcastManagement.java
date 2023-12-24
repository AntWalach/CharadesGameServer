package punsappserver;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

//Handling broadcasting messages to client sockets in waiting room
public class BroadcastManagement {

    // Method to broadcast a message to all connected client sockets
    static void broadcast(String message) {
        // Loop through each client socket in the server's list of client sockets
        for (Socket socket : CharadesGameServer.clientSockets) {
            try {
                // Create a PrintWriter to send messages to the current socket's output stream
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);

                // Send the message to the client through the socket
                socketOut.println(message);
            } catch (IOException e) {
                // Handle any IOException that might occur during the sending process
                e.printStackTrace();
            }
        }
    }

    // Method to broadcast the player count to all connected clients
    static void broadcastPlayerCount() {
        // Create a new Message object to encapsulate the player count information
        Message message = new Message();
        message.setMessageType("PLAYER_COUNT");

        // Initialize a Gson object to convert the message object to a JSON string
        Gson gson = new Gson();
        String playerCountMessage = gson.toJson(message, Message.class);

        // Broadcast the player count message to all connected clients
        broadcast(playerCountMessage);
    }
}
