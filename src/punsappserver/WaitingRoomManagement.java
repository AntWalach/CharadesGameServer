package punsappserver;

import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaitingRoomManagement {
    static final Map<Integer, String> playersMap = new ConcurrentHashMap<>();
    static int roomCount = 0;

    static void createNewRoom(){
        roomCount++;
        Message message = new Message();
        message.setMessageType("CREATE_ROOM");
        message.setX(roomCount);

        String json = new Gson().toJson(message, Message.class);
        BroadcastManagement.broadcast(json);
    }

    static void joinRoom(int roomId, String username) {
        boolean usernameExists = playersMap.containsValue(username);
        boolean sameIdSameUsername = playersMap.containsKey(roomId) && playersMap.get(roomId).equals(username);
        boolean sameUsernameDifferentId = playersMap.containsValue(username) && !sameIdSameUsername;

        if (!sameIdSameUsername && !sameUsernameDifferentId) {
            playersMap.put(roomId, username); // Dodanie gracza do pokoju, jeśli username nie istnieje w danym pokoju
            System.out.println(username + " : " + roomId);
        } else if (sameUsernameDifferentId) {
            playersMap.entrySet().removeIf(entry -> entry.getValue().equals(username)); // Usunięcie starego rekordu
            playersMap.put(roomId, username); // Dodanie gracza do nowego pokoju
            System.out.println(username + " : " + roomId);
        }
    }
}
