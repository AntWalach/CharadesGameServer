package punsappserver;

import com.google.gson.Gson;

import java.net.Socket;
import java.util.Random;

public class GameManagement {
    protected static int COUNTDOWN_SECONDS = 60;
    protected static int drawingPlayerIndex = 0;
    private static final Random random = new Random();

    static void startCountdownTimer(int roomId) {
        BroadcastManagement.broadcastLeaderboard(CharadesGameServer.playerScoresMap);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Socket currentDrawingSocket = CharadesGameServer.clientSockets.get(drawingPlayerIndex);
            CharadesGameServer.randomWord = getRandomWord(); // Initialize random word
            String username = CharadesGameServer.getUsernameForSocket(currentDrawingSocket);
            Message message = new Message();
            message.setMessageType("CHAT");
            message.setRoomId(roomId);
            message.setChat("Turn to draw: " + username);
            String json = new Gson().toJson(message);
            BroadcastManagement.broadcast(json);
            notifyDrawingPlayer(currentDrawingSocket, CharadesGameServer.randomWord, roomId);

            while (COUNTDOWN_SECONDS >= 0) {
                COUNTDOWN_SECONDS--;
                if (COUNTDOWN_SECONDS < 0) {
                    COUNTDOWN_SECONDS = 60; // Reset countdown to 1 minute
                    changeDrawingPlayer(roomId);
                    //clearChatArea();
                }
                BroadcastManagement.broadcastCountdown(COUNTDOWN_SECONDS, roomId);
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
        if (drawingPlayerIndex >= CharadesGameServer.clientSockets.size()) {
            drawingPlayerIndex = 0; // Reset to the first client socket if reached the end
        }

        // Notify the current drawing player
        Socket currentDrawingSocket = CharadesGameServer.clientSockets.get(drawingPlayerIndex);

        ChatManagement.clearChatArea(roomId);

        COUNTDOWN_SECONDS = 60;

        // Ensure a new random word that is different from the previous one
        String newRandomWord;
        do {
            newRandomWord = getRandomWord();
        } while (newRandomWord.equals(CharadesGameServer.randomWord));

        CharadesGameServer.randomWord = newRandomWord;

        CanvasManagement.broadcastColorChange("0x000000ff",currentDrawingSocket);
        notifyDrawingPlayer(currentDrawingSocket, CharadesGameServer.randomWord, roomId);
        CanvasManagement.clearCanvas();
    }

    private static void notifyDrawingPlayer(Socket drawingSocket, String word, int roomId) {
        String username = CharadesGameServer.getUsernameForSocket(drawingSocket);

        Message message = new Message();
        message.setMessageType("CHAT");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat("Turn to draw: " + username);
        String json = new Gson().toJson(message);
        BroadcastManagement.broadcast(json);

        message = new Message();
        message.setMessageType("CHAT");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat("Your word is: " + word);
        json = new Gson().toJson(message);
        CharadesGameServer.sendToClient(json, drawingSocket);

        message = new Message();
        message.setMessageType("PERMISSION");
        message.setUsername("Server");
        message.setRoomId(roomId);
        message.setChat(username);
        json = new Gson().toJson(message);
        BroadcastManagement.broadcast(json);
    }

    public static String getRandomWord() {
        return CharadesGameServer.words.get(random.nextInt(CharadesGameServer.words.size()));
    }
}
