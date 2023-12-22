package punsappserver;

import com.google.gson.Gson;

public class ChatManagement {
    static void clearChatArea(int roomId){
        Message message = new Message();
        message.setMessageType("CLEAR_CHAT");
        message.setRoomId(roomId);
        String json = new Gson().toJson(message, Message.class);

        BroadcastRoom.broadcastRoom(json);
    }
}
