package punsappserver;

import com.google.gson.Gson;

import java.net.Socket;
import java.util.Random;

//Handling game operations
public class GameManagement {
    // Variables and methods for managing game elements and turns
    protected static int drawingPlayerIndex = 0;
    private static final Random random = new Random();
    RoomServer roomServer;
    protected  String randomWord;
    GameManagement(RoomServer roomServer) {
        this.roomServer = roomServer;
    }

    // Method to start the countdown timer for drawing turns, starting game
     void startCountdownTimer(int roomId) {
         // Broadcasting leaderboard to all clients
        BroadcastRoom.broadcastLeaderboard(roomServer.playerScoresMap, roomId, roomServer);

        new Thread(() -> {
            try {
                Thread.sleep(3000); //Wait before starting round
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Retrieve the socket of the current drawing player
            Socket currentDrawingSocket = roomServer.clientSockets.get(drawingPlayerIndex);
            randomWord = getRandomWord(); // Initialize random word
            String username = roomServer.getUsernameForSocket(currentDrawingSocket);


            Message message = new Message();
            message.setMessageType("TURN_INFO");
            message.setUsername("Server");
            message.setRoomId(roomId);
            message.setChat(username);
            String json = new Gson().toJson(message);

            BroadcastRoom.broadcastRoom(json, roomServer); // Broadcast turn information
            notifyDrawingPlayer(currentDrawingSocket, randomWord, roomId); // Notify the drawing player

            while (roomServer.COUNTDOWN_SECONDS >= 0) {
                roomServer.COUNTDOWN_SECONDS--;
                if (roomServer.COUNTDOWN_SECONDS < 0) {
                    roomServer.COUNTDOWN_SECONDS = 60; // Reset countdown to 1 minute
                    changeDrawingPlayer(roomId); //Change drawing player
                }
                BroadcastRoom.broadcastCountdown(roomServer.COUNTDOWN_SECONDS, roomId, roomServer);
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Method to change the drawing player for the next turn
     void changeDrawingPlayer(int roomId) {
        drawingPlayerIndex++;
        if (drawingPlayerIndex >= roomServer.clientSockets.size()) {
            drawingPlayerIndex = 0; // Reset to the first client socket if reached the end
        }

         // Retrieve the socket of the current drawing player
        Socket currentDrawingSocket = roomServer.clientSockets.get(drawingPlayerIndex);

         // Clear elements for the new turn
        CanvasManagement.clearWordLabel(roomId, roomServer);
        ChatManagement.clearChatArea(roomId, roomServer);

        roomServer.COUNTDOWN_SECONDS = 60;

        // Ensure a new random word that is different from the previous one
        String newRandomWord;
        do {
            newRandomWord = getRandomWord();
        } while (newRandomWord.equals(randomWord));

        randomWord = newRandomWord;

       // Broadcast changes for the new drawing player and all clients
         CanvasManagement.broadcastColorChange("0x000000ff",currentDrawingSocket, roomId, roomServer);
         notifyDrawingPlayer(currentDrawingSocket, randomWord, roomId);
         CanvasManagement.clearCanvas(roomId, roomServer);
    }

    // Method to inform the drawing player and others about the turn and the word to draw
    private  void notifyDrawingPlayer(Socket drawingSocket, String word, int roomId) {
        String username = roomServer.getUsernameForSocket(drawingSocket);

        // Message for informing clients about the turn
        Message message = new Message();
        message.setMessageType("TURN_INFO");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat(username);
        String json = new Gson().toJson(message);
        BroadcastRoom.broadcastRoom(json, roomServer);

        // Message for sending the word to the drawing player only
        message = new Message();
        message.setMessageType("WORD_INFO");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat(word);
        json = new Gson().toJson(message);
        RoomServer.sendToClient(json, drawingSocket);

        // Permission message for the drawing player to start drawing
        message = new Message();
        message.setMessageType("PERMISSION");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat(username);
        json = new Gson().toJson(message);
        BroadcastRoom.broadcastRoom(json, roomServer);
    }

    public static String getRandomWord() {
        return RoomServer.words.get(random.nextInt(RoomServer.words.size()));
    }

    // Method to handle chat messages received during the game
    public  void onChatMessageReceived(String username, String chatMessage, int roomId, RoomServer roomServer) {
        String trimmedChatMessage = chatMessage.trim().toLowerCase();

        if (trimmedChatMessage.equals(randomWord.toLowerCase())) {
            // Handle guessed word message
            Message guessedWordMessage = new Message();
            guessedWordMessage.setMessageType("CHAT");
            guessedWordMessage.setUsername("Server");
            guessedWordMessage.setRoomId(roomId);
            guessedWordMessage.setChat(username + " guessed the word! - " + randomWord);

            String winMessage = new Gson().toJson(guessedWordMessage);

            Socket userSocket = roomServer.userSocketMap.get(username);

            if (userSocket != null) {
                // Increment the score for the user's socket
                roomServer.playerScoresMap.merge(userSocket, 1, Integer::sum);
            }

            // Change drawing player
            changeDrawingPlayer(roomId);
            BroadcastRoom.broadcastClearLeaderboard(roomId, roomServer);
            BroadcastRoom.broadcastRoom(winMessage, roomServer);
            BroadcastRoom.broadcastLeaderboard(roomServer.playerScoresMap, roomId, roomServer);
        } else {
            // Handle regular chat messages
            Message regularChatMessage = new Message();
            regularChatMessage.setMessageType("CHAT");
            regularChatMessage.setRoomId(roomId);
            regularChatMessage.setUsername(username);
            regularChatMessage.setChat(chatMessage);
            String regularChatJson = new Gson().toJson(regularChatMessage);
            BroadcastRoom.broadcastRoom(regularChatJson, roomServer);
        }
    }
}
