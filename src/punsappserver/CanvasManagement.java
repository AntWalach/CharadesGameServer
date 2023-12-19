package punsappserver;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CanvasManagement {
    private static final long CLEAR_COOLDOWN = 1000;
    private static long lastClearTime = 0;

    public static void onClearCanvasReceived(String message) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClearTime > CLEAR_COOLDOWN) {
            BroadcastManagement.broadcast(message);
            lastClearTime = currentTime;
        }
    }

    public static void onClearCanvasReceived1(String message) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClearTime > CLEAR_COOLDOWN) {
            BroadcastManagement.broadcast(message);
            lastClearTime = currentTime;
        }
    }

    public static void onColorReceived(String messageServer) {
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

    static void broadcastColorChange(String color, Socket senderSocket) {
        for (Socket socket : CharadesGameServer.clientSockets) {
            if (!socket.equals(senderSocket)) {
                try {
                    PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);

                    Message colorMessage = new Message();
                    colorMessage.setUsername("Server");
                    colorMessage.setMessageType("COLOR_CHANGE");
                    colorMessage.setColor(color);

                    String json = new Gson().toJson(colorMessage);
                    socketOut.println(json);

                    System.out.println("Sent color change to " + CharadesGameServer.getUsernameForSocket(socket) + ": " + color);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void clearCanvas() {
        Message message = new Message();
        message.setMessageType("CLEAR_CANVAS");
        String json = new Gson().toJson(message);
        CanvasManagement.onClearCanvasReceived1(json);
    }

    static void setClientColor(String color, Socket clientSocket) {
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

}
