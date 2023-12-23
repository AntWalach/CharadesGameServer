package punsappserver;

import com.google.gson.Gson;

import java.net.Socket;
import java.util.Random;

public class GameManagement {
    //protected static int COUNTDOWN_SECONDS = 60;
    protected static int drawingPlayerIndex = 0;
    private static final Random random = new Random();
    RoomServer roomServer;
    protected  String randomWord;
    GameManagement(RoomServer roomServer) {
        this.roomServer = roomServer;
    }

     void startCountdownTimer(int roomId) {
        BroadcastRoom.broadcastLeaderboard(roomServer.playerScoresMap, roomId, roomServer);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Socket currentDrawingSocket = roomServer.clientSockets.get(drawingPlayerIndex);
            randomWord = getRandomWord(); // Initialize random word
            String username = roomServer.getUsernameForSocket(currentDrawingSocket);
            Message message = new Message();
            message.setMessageType("CHAT");
            message.setRoomId(roomId);
            message.setChat("Turn to draw: " + username);
            String json = new Gson().toJson(message);
            BroadcastRoom.broadcastRoom(json, roomServer);
            notifyDrawingPlayer(currentDrawingSocket, randomWord, roomId);

            while (roomServer.COUNTDOWN_SECONDS >= 0) {
                roomServer.COUNTDOWN_SECONDS--;
                if (roomServer.COUNTDOWN_SECONDS < 0) {
                    roomServer.COUNTDOWN_SECONDS = 60; // Reset countdown to 1 minute
                    changeDrawingPlayer(roomId);
                    //clearChatArea();
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

     void changeDrawingPlayer(int roomId) {
        drawingPlayerIndex++;
        if (drawingPlayerIndex >= roomServer.clientSockets.size()) {
            drawingPlayerIndex = 0; // Reset to the first client socket if reached the end
        }

        // Notify the current drawing player
        Socket currentDrawingSocket = roomServer.clientSockets.get(drawingPlayerIndex);

        ChatManagement.clearChatArea(roomId, roomServer);

        roomServer.COUNTDOWN_SECONDS = 60;

        // Ensure a new random word that is different from the previous one
        String newRandomWord;
        do {
            newRandomWord = getRandomWord();
        } while (newRandomWord.equals(randomWord));

       randomWord = newRandomWord;

        CanvasManagement.broadcastColorChange("0x000000ff",currentDrawingSocket, roomId, roomServer);
        notifyDrawingPlayer(currentDrawingSocket, randomWord, roomId);
        CanvasManagement.clearCanvas(roomId, roomServer);
    }

    private  void notifyDrawingPlayer(Socket drawingSocket, String word, int roomId) {
        String username = roomServer.getUsernameForSocket(drawingSocket);

        Message message = new Message();
        message.setMessageType("CHAT");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat("Turn to draw: " + username);
        String json = new Gson().toJson(message);
        BroadcastRoom.broadcastRoom(json, roomServer);

        message = new Message();
        message.setMessageType("CHAT");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat("Your word is: " + word);
        json = new Gson().toJson(message);
        RoomServer.sendToClient(json, drawingSocket);

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
            //clearChatArea();
        } else {
            // Handle regular chat messages
            Message regularChatMessage = new Message();
            regularChatMessage.setMessageType("CHAT");
            regularChatMessage.setRoomId(roomId);
            regularChatMessage.setUsername(username);
            regularChatMessage.setChat(chatMessage);
            String regularChatJson = new Gson().toJson(regularChatMessage);
            //BroadcastManagement.broadcast(regularChatJson);
            BroadcastRoom.broadcastRoom(regularChatJson, roomServer);
        }
    }
}
