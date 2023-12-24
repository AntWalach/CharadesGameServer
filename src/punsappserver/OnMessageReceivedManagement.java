package punsappserver;

//Handling received messages from clients
public class OnMessageReceivedManagement {
    public static void onMessageReceived(String message) {
        // Broadcast chat message to all clients in waiting room
        BroadcastManagement.broadcast(message);
    }

    public static void onMessageReceivedRoom(String message, RoomServer roomServer) {
        // Broadcast chat message to all clients in room
        BroadcastRoom.broadcastRoom(message, roomServer);
    }
}
