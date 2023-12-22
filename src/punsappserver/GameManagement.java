package punsappserver;

import com.google.gson.Gson;

import java.net.Socket;
import java.util.Random;

public class GameManagement {
    //protected static int COUNTDOWN_SECONDS = 60;
    protected static int drawingPlayerIndex = 0;
    private static final Random random = new Random();

    static void startCountdownTimer(int roomId) {
        BroadcastRoom.broadcastLeaderboard(RoomServer.playerScoresMap, roomId);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Socket currentDrawingSocket = RoomServer.clientSockets.get(drawingPlayerIndex);
            RoomServer.randomWord = getRandomWord(); // Initialize random word
            String username = RoomServer.getUsernameForSocket(currentDrawingSocket);
            Message message = new Message();
            message.setMessageType("CHAT");
            message.setRoomId(roomId);
            message.setChat("Turn to draw: " + username);
            String json = new Gson().toJson(message);
            BroadcastRoom.broadcastRoom(json);
            notifyDrawingPlayer(currentDrawingSocket, RoomServer.randomWord, roomId);

            while (RoomServer.COUNTDOWN_SECONDS >= 0) {
                RoomServer.COUNTDOWN_SECONDS--;
                if (RoomServer.COUNTDOWN_SECONDS < 0) {
                    RoomServer.COUNTDOWN_SECONDS = 60; // Reset countdown to 1 minute
                    changeDrawingPlayer(roomId);
                    //clearChatArea();
                }
                BroadcastRoom.broadcastCountdown(RoomServer.COUNTDOWN_SECONDS, roomId);
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    static void changeDrawingPlayer(int roomId) {
        drawingPlayerIndex++;
        if (drawingPlayerIndex >= RoomServer.clientSockets.size()) {
            drawingPlayerIndex = 0; // Reset to the first client socket if reached the end
        }

        // Notify the current drawing player
        Socket currentDrawingSocket = RoomServer.clientSockets.get(drawingPlayerIndex);

        ChatManagement.clearChatArea(roomId);

        RoomServer.COUNTDOWN_SECONDS = 60;

        // Ensure a new random word that is different from the previous one
        String newRandomWord;
        do {
            newRandomWord = getRandomWord();
        } while (newRandomWord.equals(RoomServer.randomWord));

        RoomServer.randomWord = newRandomWord;

        CanvasManagement.broadcastColorChange("0x000000ff",currentDrawingSocket, roomId);
        notifyDrawingPlayer(currentDrawingSocket, RoomServer.randomWord, roomId);
        CanvasManagement.clearCanvas(roomId);
    }

    private static void notifyDrawingPlayer(Socket drawingSocket, String word, int roomId) {
        String username = RoomServer.getUsernameForSocket(drawingSocket);

        Message message = new Message();
        message.setMessageType("CHAT");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat("Turn to draw: " + username);
        String json = new Gson().toJson(message);
        BroadcastRoom.broadcastRoom(json);

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
        BroadcastRoom.broadcastRoom(json);
    }

    public static String getRandomWord() {
        return RoomServer.words.get(random.nextInt(RoomServer.words.size()));
    }
}
