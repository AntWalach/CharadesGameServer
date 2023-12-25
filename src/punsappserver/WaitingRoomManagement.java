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
    static final Map<Integer, Long> roomPlayerCounts = new HashMap<>();
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
    static void joinRoom(int newRoomId, String username) {
        // Check if the user is already in a room
        if (playersMap.containsKey(username)) {
            int currentRoomId = playersMap.get(username);

            // Decrement player count in the current room
            decrementPlayerCount(currentRoomId);

            // Update the user's room to the new room
            playersMap.put(username, newRoomId);

            // Increment player count in the new room
            incrementPlayerCount(newRoomId);
        } else {
            // If the user is not in any room, simply assign them to the new room
            playersMap.put(username, newRoomId);

            // Increment player count in the new room
            incrementPlayerCount(newRoomId);
        }

        // Update player counts in each room
        countPlayersForEachRoom();
    }

    // Method to decrement player count in a specific room
    static void decrementPlayerCount(int roomId) {
        if (roomPlayerCounts.containsKey(roomId)) {
            roomPlayerCounts.put(roomId, roomPlayerCounts.get(roomId) - 1);
        }
    }

    // Method to increment player count in a specific room
    static void incrementPlayerCount(int roomId) {
        roomPlayerCounts.put(roomId, roomPlayerCounts.getOrDefault(roomId, 0L) + 1);
    }

    // Method to count the number of players in each room and broadcast the counts
    static void countPlayersForEachRoom() {
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
