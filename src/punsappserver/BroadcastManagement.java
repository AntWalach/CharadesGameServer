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
        message.setX(RoomServer.clientSockets.size());

        Gson gson = new Gson();
        String playerCountMessage = gson.toJson(message, Message.class);

        broadcast(playerCountMessage);
    }


}
