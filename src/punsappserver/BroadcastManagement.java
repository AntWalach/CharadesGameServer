package punsappserver;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastManagement {
    static void broadcast(String message) {
        for (Socket socket : CharadesGameServer.clientSockets) {
            try {
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                socketOut.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void broadcastPlayerCount() {
        Message message = new Message();
        message.setMessageType("PLAYER_COUNT");
        message.setX(CharadesGameServer.clientSockets.size());

        Gson gson = new Gson();
        String playerCountMessage = gson.toJson(message, Message.class);

        broadcast(playerCountMessage);
    }

    static void broadcastCountdown(int countdown, int roomId) {
        Message message = new Message();
        message.setMessageType("COUNTDOWN");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setX(countdown);

        Gson gson = new Gson();
        String countdownMessage = gson.toJson(message, Message.class);

        broadcast(countdownMessage);
    }

    static void broadcastLeaderboard(Map<Socket, Integer> playerScoresMap) {
        for (Map.Entry<Socket, Integer> entry : playerScoresMap.entrySet()) {
            Socket socket = entry.getKey();
            Integer score = entry.getValue();

            // Convert Socket to username (or any identifier)
            String username = CharadesGameServer.getUsernameForSocket(socket);

            Message message =new Message();
            message.setMessageType("LEADERBOARD");
            message.setUsername(username);
            message.setX(score);

            String json = new Gson().toJson(message, Message.class);
            BroadcastManagement.broadcast(json);
        }
    }

    static void broadcastClearLeaderboard(){
        Message message =new Message();
        message.setMessageType("CLEAR_LEADERBOARD");
        String json = new Gson().toJson(message, Message.class);
        BroadcastManagement.broadcast(json);
    }
}
