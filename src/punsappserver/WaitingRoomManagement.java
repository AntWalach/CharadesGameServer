package punsappserver;

import com.google.gson.Gson;

import java.util.HashMap;
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

        // Update player counts in each room
        countPlayersForEachRoom();
    }

    // Method to count the number of players in each room and broadcast the counts
    static void countPlayersForEachRoom() {
        Map<Integer, Long> roomPlayerCounts = new HashMap<>();

        // Calculate player counts for each room
        for (Map.Entry<String, Integer> entry : playersMap.entrySet()) {
            int roomId = entry.getValue();
            roomPlayerCounts.put(roomId, roomPlayerCounts.getOrDefault(roomId, 0L) + 1);
        }

        // Broadcast the player counts for each room
        for (Map.Entry<Integer, Long> roomEntry : roomPlayerCounts.entrySet()) {
            int roomId = roomEntry.getKey();
            long roomSize = roomEntry.getValue();

            // Prepare message for broadcasting player count update in a room
            Message message = new Message();
            message.setRoomId(roomId);
            message.setMessageType("PLAYERS_COUNT_UPDATE");
            message.setY(roomSize); // Assign the player count for the room

            String json = new Gson().toJson(message, Message.class);
            BroadcastManagement.broadcast(json); // Broadcast player count update
        }
    }
}
