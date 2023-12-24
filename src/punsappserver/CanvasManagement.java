package punsappserver;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


//Operations handling canvas
public class CanvasManagement {
    // Constants and variables for managing the clearing of the canvas with a cooldown period
    private static final long CLEAR_COOLDOWN = 1000;
    private static long lastClearTime = 0;

    // Method to handle the event when a request to clear the canvas is received
    public static void onClearCanvasReceived(String message, RoomServer roomServer) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClearTime > CLEAR_COOLDOWN) {
            // Broadcast the clear canvas message to all clients in the room
            BroadcastRoom.broadcastRoom(message, roomServer);
            lastClearTime = currentTime;
        }
    }

    // Method to handle the event when a color change request is received
    public static void onColorReceived(String messageServer, RoomServer roomServer) {
        // Convert the received message to a Message object using Gson
        Gson gson = new Gson();
        Message colorMessage = gson.fromJson(messageServer, Message.class);

        // Extract necessary information from the received color change message
        String username = colorMessage.getUsername();
        String color = colorMessage.getColor();
        int roomId = colorMessage.getRoomId();
        Socket senderSocket = roomServer.getSocketForUser(username);

        // Broadcast the color change to other clients in the room
        broadcastColorChange(color, senderSocket, roomId, roomServer);

        // Set the color for the sender client
        setClientColor(color, senderSocket, roomId);
    }

    // Method to broadcast a color change to other clients in the room
    static void broadcastColorChange(String color, Socket senderSocket, int roomId, RoomServer roomServer) {
        for (Socket socket : roomServer.clientSockets) {
            if (!socket.equals(senderSocket)) {
                try {
                    // Create a message for the color change
                    Message colorMessage = new Message();
                    colorMessage.setUsername("Server");
                    colorMessage.setRoomId(roomId);
                    colorMessage.setMessageType("COLOR_CHANGE");
                    colorMessage.setColor(color);

                    // Convert the color change message to JSON and send it to the client socket
                    PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                    String json = new Gson().toJson(colorMessage);
                    socketOut.println(json);

                    // Log the sent color change message
                    System.out.println("Sent color change to " + roomServer.getUsernameForSocket(socket) + ": " + color);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Method to broadcast a clear canvas message to all clients in the room
    public static void clearCanvas(int roomId, RoomServer roomServer) {
        Message message = new Message();
        message.setMessageType("CLEAR_CANVAS");
        message.setRoomId(roomId);
        String json = new Gson().toJson(message);
        BroadcastRoom.broadcastRoom(json, roomServer);
    }

    // Method to broadcast a clear word label message to all clients in the room
    public static void clearWordLabel(int roomId, RoomServer roomServer) {
        Message message = new Message();
        message.setMessageType("CLEAR_WORD_LABEL");
        message.setRoomId(roomId);
        String json = new Gson().toJson(message);
        BroadcastRoom.broadcastRoom(json, roomServer);
    }

    // Method to set the color for a specific client
    static void setClientColor(String color, Socket clientSocket, int roomId) {
        try {
            // Create a message for the color change
            Message colorMessage = new Message();
            colorMessage.setUsername("Server");
            colorMessage.setRoomId(roomId);
            colorMessage.setMessageType("COLOR_CHANGE");
            colorMessage.setColor(color);

            // Convert the color change message to JSON and send it to the client socket
            PrintWriter socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
            String json = new Gson().toJson(colorMessage);
            socketOut.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
