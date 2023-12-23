package punsappserver;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class RoomClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private boolean countdownStarted = false;
    private String username;
    RoomServer roomServer;

    GameManagement gameManagement;

    public RoomClientHandler(Socket clientSocket, RoomServer roomServer, GameManagement gameManagement) {
        this.clientSocket = clientSocket;
        this.roomServer = roomServer;
        this.gameManagement = gameManagement;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String messageServer;
            while ((messageServer = in.readLine()) != null) {
                System.out.println("Received message: " + messageServer);

                Gson gson = new Gson();

                Message message = gson.fromJson(messageServer, Message.class);

                if (Objects.equals(message.getMessageType(), "CLEAR_CANVAS")) {
                    CanvasManagement.onClearCanvasReceived(messageServer, roomServer);
                } else if (Objects.equals(message.getMessageType(), "START") && !countdownStarted) {
                    int roomId = message.getRoomId();
                    int roomPort = 3000 + roomId;
                    handleMessage(messageServer);
                    countdownStarted = true;
                    //OnMessageReceivedManagement.onCountdownStartReceived(roomId);
                    //GameManagement.startCountdownTimer(roomId);
                    RoomServer roomServer = new RoomServer(roomPort);
                    roomServer.run();
                    //GameManagement.startCountdownTimer(roomId);
                } else if (Objects.equals(message.getMessageType(), "SET_USERNAME")) {
                    username = message.getUsername();
                    roomServer.addUser(username, clientSocket);
                } else if (Objects.equals(message.getMessageType(), "COLOR_CHANGE")) {
                    CanvasManagement.onColorReceived(messageServer, roomServer);
                } else if (Objects.equals(message.getMessageType(), "CHAT")){
                    gameManagement.onChatMessageReceived(username, message.getChat(), message.getRoomId(), roomServer);
                } else {
                    handleMessage(messageServer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeClientSocket();
        }
    }

    private void handleMessage(String message) {
        OnMessageReceivedManagement.onMessageReceivedRoom(message, roomServer);
    }

    private void closeClientSocket() {
        try {
            if (username != null && !username.isEmpty()) {
                roomServer.removeUser(username);
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}