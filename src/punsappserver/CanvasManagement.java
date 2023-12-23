package punsappserver;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CanvasManagement {
    private static final long CLEAR_COOLDOWN = 1000;
    private static long lastClearTime = 0;

    public static void onClearCanvasReceived(String message, RoomServer roomServer) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClearTime > CLEAR_COOLDOWN) {
            BroadcastRoom.broadcastRoom(message, roomServer);
            lastClearTime = currentTime;
        }
    }


    public static void onColorReceived(String messageServer, RoomServer roomServer) {
        Gson gson = new Gson();
        Message colorMessage = gson.fromJson(messageServer, Message.class);

        String username = colorMessage.getUsername();
        String color = colorMessage.getColor();
        int roomId = colorMessage.getRoomId();
        Socket senderSocket = roomServer.getSocketForUser(username);

        // Broadcast the color change to other clients
        broadcastColorChange(color, senderSocket,roomId,roomServer);

        // Set the color for the sender client
        setClientColor(color, senderSocket, roomId);
    }

    static void broadcastColorChange(String color, Socket senderSocket,int roomId, RoomServer roomServer) {
        for (Socket socket : roomServer.clientSockets) {
            if (!socket.equals(senderSocket)) {
                try {
                    PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);

                    Message colorMessage = new Message();
                    colorMessage.setUsername("Server");
                    colorMessage.setRoomId(roomId);
                    colorMessage.setMessageType("COLOR_CHANGE");
                    colorMessage.setColor(color);

                    String json = new Gson().toJson(colorMessage);
                    socketOut.println(json);

                    System.out.println("Sent color change to " + roomServer.getUsernameForSocket(socket) + ": " + color);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void clearCanvas(int roomId, RoomServer roomServer) {
        Message message = new Message();
        message.setMessageType("CLEAR_CANVAS");
        message.setRoomId(roomId);
        String json = new Gson().toJson(message);
        BroadcastRoom.broadcastRoom(json,roomServer);
    }

    public static void clearWordLabel(int roomId, RoomServer roomServer) {
        Message message = new Message();
        message.setMessageType("CLEAR_WORD_LABEL");
        message.setRoomId(roomId);
        String json = new Gson().toJson(message);
        BroadcastRoom.broadcastRoom(json,roomServer);
    }

    static void setClientColor(String color, Socket clientSocket, int roomId) {
        try {
            PrintWriter socketOut = new PrintWriter(clientSocket.getOutputStream(), true);

            Message colorMessage = new Message();
            colorMessage.setUsername("Server");
            colorMessage.setRoomId(roomId);
            colorMessage.setMessageType("COLOR_CHANGE");
            colorMessage.setColor(color);

            String json = new Gson().toJson(colorMessage);
            socketOut.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
