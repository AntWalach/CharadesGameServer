package punsappserver;

import com.google.gson.Gson;

import java.net.Socket;

public class OnMessageReceivedManagement {

    public static void onMessageReceived(String message) {
        // Broadcast chat message to all clients
        BroadcastManagement.broadcast(message);
    }

    public static void onCountdownStartReceived(int roomId) {
        CharadesGameServer.countdownRunning = true;
        GameManagement.startCountdownTimer(roomId);
    }

    public static void onChatMessageReceived(String username, String chatMessage, int roomId) {
//        String trimmedChatMessage = chatMessage.trim().toLowerCase();
//
//        if (trimmedChatMessage.equals(CharadesGameServer.randomWord.toLowerCase())) {
//            // Handle guessed word message
//            Message guessedWordMessage = new Message();
//            guessedWordMessage.setMessageType("CHAT");
//            guessedWordMessage.setUsername("Server");
//            guessedWordMessage.setRoomId(roomId);
//            guessedWordMessage.setChat(username + " guessed the word! - " + CharadesGameServer.randomWord);
//
//            String winMessage = new Gson().toJson(guessedWordMessage);
//
//            Socket userSocket = CharadesGameServer.userSocketMap.get(username);
//
//            if (userSocket != null) {
//                // Increment the score for the user's socket
//                CharadesGameServer.playerScoresMap.merge(userSocket, 1, Integer::sum);
//            }
//
//            // Change drawing player
//            GameManagement.changeDrawingPlayer(roomId);
//            BroadcastManagement.broadcastClearLeaderboard();
//            BroadcastManagement.broadcast(winMessage);
//            BroadcastManagement.broadcastLeaderboard(CharadesGameServer.playerScoresMap);
//            //clearChatArea();
//        } else {
            // Handle regular chat messages
            Message regularChatMessage = new Message();
            regularChatMessage.setMessageType("CHAT");
            regularChatMessage.setRoomId(roomId);
            regularChatMessage.setUsername(username);
            regularChatMessage.setChat(chatMessage);
            String regularChatJson = new Gson().toJson(regularChatMessage);
            //BroadcastManagement.broadcast(regularChatJson);
            BroadcastRoom.broadcastRoom(regularChatJson);
        }
   // }
}
