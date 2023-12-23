package punsappserver;

import com.google.gson.Gson;

import java.net.Socket;

public class OnMessageReceivedManagement {

    public static void onMessageReceived(String message) {
        // Broadcast chat message to all clients
        BroadcastManagement.broadcast(message);

    }

    public static void onMessageReceivedRoom(String message, RoomServer roomServer) {
        BroadcastRoom.broadcastRoom(message, roomServer);
    }
}
