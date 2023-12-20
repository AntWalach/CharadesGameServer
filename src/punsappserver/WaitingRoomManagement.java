package punsappserver;

import com.google.gson.Gson;

public class WaitingRoomManagement {
    static void createNewRoom(){
        Message message = new Message();
        message.setMessageType("CREATE_ROOM");

        String json = new Gson().toJson(message, Message.class);
        BroadcastManagement.broadcast(json);
    }
}
