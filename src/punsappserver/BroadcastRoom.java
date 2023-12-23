package punsappserver;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class BroadcastRoom {


    static void broadcastRoom(String message, RoomServer roomServer) {
        for (Socket socket : roomServer.clientSockets) {
            try {
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                socketOut.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void broadcastCountdown(int countdown, int roomId, RoomServer roomServer) {
        Message message = new Message();
        message.setMessageType("COUNTDOWN");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setX(countdown);

        Gson gson = new Gson();
        String countdownMessage = gson.toJson(message, Message.class);

        broadcastRoom(countdownMessage, roomServer);
    }

    static void broadcastLeaderboard(Map<Socket, Integer> playerScoresMap,int roomId, RoomServer roomServer) {
        for (Map.Entry<Socket, Integer> entry : playerScoresMap.entrySet()) {
            Socket socket = entry.getKey();
            Integer score = entry.getValue();

            // Convert Socket to username (or any identifier)
            String username = roomServer.getUsernameForSocket(socket);

            Message message =new Message();
            message.setMessageType("LEADERBOARD");
            message.setUsername(username);
            message.setRoomId(roomId);
            message.setX(score);

            String json = new Gson().toJson(message, Message.class);
            broadcastRoom(json, roomServer);
        }
    }

    static void broadcastClearLeaderboard(int roomId, RoomServer roomServer){
        Message message =new Message();
        message.setMessageType("CLEAR_LEADERBOARD");
        message.setRoomId(roomId);
        String json = new Gson().toJson(message, Message.class);
        BroadcastRoom.broadcastRoom(json, roomServer);
    }
}
