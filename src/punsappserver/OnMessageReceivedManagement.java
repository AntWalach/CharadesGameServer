package punsappserver;

import com.google.gson.Gson;

public class OnMessageReceivedManagement {

    public static void onMessageReceived(String message) {
        // Broadcast chat message to all clients
        BroadcastManagement.broadcast(message);
    }

    public static void onCountdownStartReceived() {
        CharadesGameServer.countdownRunning = true;
        GameManagement.startCountdownTimer();
    }

    public static void onChatMessageReceived(String username, String chatMessage) {
        String trimmedChatMessage = chatMessage.trim().toLowerCase();

        if (trimmedChatMessage.equals(CharadesGameServer.randomWord.toLowerCase())) {
            // Handle guessed word message
            Message guessedWordMessage = new Message();
            guessedWordMessage.setMessageType("CHAT");
            guessedWordMessage.setUsername("Server");
            guessedWordMessage.setChat(username + " guessed the word! - " + CharadesGameServer.randomWord);

            String winMessage = new Gson().toJson(guessedWordMessage);
            BroadcastManagement.broadcast(winMessage);

            // Change drawing player
            GameManagement.changeDrawingPlayer();
            //clearChatArea();
        } else {
            // Handle regular chat messages
            Message regularChatMessage = new Message();
            regularChatMessage.setMessageType("CHAT");
            regularChatMessage.setUsername(username);
            regularChatMessage.setChat(chatMessage);
            String regularChatJson = new Gson().toJson(regularChatMessage);
            BroadcastManagement.broadcast(regularChatJson);
        }
    }
}
