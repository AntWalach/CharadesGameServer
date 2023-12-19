package punsappserver;

import com.google.gson.Gson;

public class ChatManagement {
    static void clearChatArea(){
        Message message = new Message();
        message.setMessageType("CLEAR_CHAT");
        String json = new Gson().toJson(message, Message.class);

        BroadcastManagement.broadcast(json);
    }
}
