package punsappserver;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

//Handling broadcasting messages to client sockets in separate game rooms
public class BroadcastRoom {
    // Method to broadcast a message to all clients within a room
    static void broadcastRoom(String message, RoomServer roomServer) {
        // Loop through each client socket in the room's clientSockets list
        for (Socket socket : roomServer.clientSockets) {
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

    // Method to broadcast a countdown to all clients within a room
    static void broadcastCountdown(int countdown, int roomId, RoomServer roomServer) {
        // Create a message object for the countdown
        Message message = new Message();
        message.setMessageType("COUNTDOWN");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setX(countdown);

        // Convert the message to JSON format
        Gson gson = new Gson();
        String countdownMessage = gson.toJson(message, Message.class);

        // Broadcast the countdown message to all clients in the room
        broadcastRoom(countdownMessage, roomServer);
    }

    // Method to broadcast the leaderboard to all clients within a room
    static void broadcastLeaderboard(Map<Socket, Integer> playerScoresMap, int roomId, RoomServer roomServer) {
        // Iterate through playerScoresMap to retrieve player scores and corresponding sockets
        for (Map.Entry<Socket, Integer> entry : playerScoresMap.entrySet()) {
            Socket socket = entry.getKey();
            Integer score = entry.getValue();

            // Retrieve username associated with the socket
            String username = roomServer.getUsernameForSocket(socket);

            // Create a message for each player's score on the leaderboard
            Message message = new Message();
            message.setMessageType("LEADERBOARD");
            message.setUsername(username);
            message.setRoomId(roomId);
            message.setX(score);

            // Convert the message to JSON and broadcast it to all clients in the room
            String json = new Gson().toJson(message, Message.class);
            broadcastRoom(json, roomServer);
        }
    }

    // Method to broadcast the clearing of the leaderboard to all clients within a room
    static void broadcastClearLeaderboard(int roomId, RoomServer roomServer) {
        // Create a message to clear the leaderboard
        Message message = new Message();
        message.setMessageType("CLEAR_LEADERBOARD");
        message.setRoomId(roomId);

        // Convert the message to JSON and broadcast it to all clients in the room
        String json = new Gson().toJson(message, Message.class);
        BroadcastRoom.broadcastRoom(json, roomServer);
    }
}
