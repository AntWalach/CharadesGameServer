package punsappserver;

import com.google.gson.Gson;

//Handling chat operations
public class ChatManagement {

    // Method to clear the chat area for a specific room
    static void clearChatArea(int roomId, RoomServer roomServer){
        // Create a message to indicate clearing the chat
        Message message = new Message();
        message.setMessageType("CLEAR_CHAT");
        message.setRoomId(roomId);

        // Convert the message to JSON format using Gson
        String json = new Gson().toJson(message, Message.class);

        // Broadcast the clear chat message to all clients in the room
        BroadcastRoom.broadcastRoom(json, roomServer);
    }
}
