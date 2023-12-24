package punsappserver;

import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//Operations for waiting room
public class WaitingRoomManagement {
    // Map to track players and their assigned rooms
    static final Map<String, Integer> playersMap = new ConcurrentHashMap<>();
    static int roomCount = 0; // Counter to keep track of created rooms

    // Method to create a new room and broadcast its creation
    static void createNewRoom() {
        roomCount++;
        Message message = new Message();
        message.setMessageType("CREATE_ROOM");
        message.setX(roomCount); // Assigning a room count identifier

        String json = new Gson().toJson(message, Message.class);
        BroadcastManagement.broadcast(json); // Broadcast message about room creation
    }

    // Method to add a player to a specific room
    static void joinRoom(int roomId, String username) {
        playersMap.put(username, roomId); // Assigning a player to a room

        countPlayersForEachRoom(); // Update player counts in each room
    }

    // Method to count the number of players in each room and broadcast the counts
    static void countPlayersForEachRoom() {
        Set<Integer> uniqueRoomIds = new HashSet<>(playersMap.values());

        for (int roomId : uniqueRoomIds) {
            long roomSize = playersMap.values().stream()
                    .filter(value -> value == roomId)
                    .count();

            // Prepare message for broadcasting player count update in a room
            Message message = new Message();
            message.setRoomId(roomId);
            message.setMessageType("PLAYERS_COUNT_UPDATE");
            message.setX(roomSize); // Assign the player count for the room

            String json = new Gson().toJson(message, Message.class);
            BroadcastManagement.broadcast(json); // Broadcast player count update
        }
    }
}
