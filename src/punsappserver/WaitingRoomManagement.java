package punsappserver;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WaitingRoomManagement {
    static final Map<String, Integer> playersMap = new ConcurrentHashMap<>();
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
        playersMap.put(username, roomId);

        countPlayersForEachRoom();
    }


    static void countPlayersForEachRoom() {
        Set<Integer> uniqueRoomIds = new HashSet<>(playersMap.values());

        for (int roomId : uniqueRoomIds) {
            long roomSize = playersMap.values().stream()
                    .filter(value -> value == roomId)
                    .count();

            //System.out.println("Liczba graczy w pokoju o id " + roomId + ": " + roomSize);

            Message message = new Message();
            message.setRoomId(roomId);
            message.setMessageType("PLAYERS_COUNT_UPDATE");
            message.setX(roomSize);
            String json = new Gson().toJson(message, Message.class);
            BroadcastManagement.broadcast(json);
        }
    }


}
