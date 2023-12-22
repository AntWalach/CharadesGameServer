package punsappserver;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private boolean countdownStarted = false;
    private String username;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
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

                if (Objects.equals(message.getMessageType(), "START")) {
                    int roomId = message.getRoomId();
                    int roomPort = 3000 + roomId;
                    handleMessage(messageServer);
                    RoomServer roomServer = new RoomServer(roomPort);
                    roomServer.run();
                } else if (Objects.equals(message.getMessageType(), "SET_USERNAME")) {
                    username = message.getUsername();
                    CharadesGameServer.addUser(username, clientSocket);
                }  else if (Objects.equals(message.getMessageType(), "CREATE_ROOM")) {
                    WaitingRoomManagement.createNewRoom();
                } else if (Objects.equals(message.getMessageType(), "JOIN_ROOM")) {
                    WaitingRoomManagement.joinRoom(message.roomId, message.getUsername());
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
        OnMessageReceivedManagement.onMessageReceived(message);
    }

    private void closeClientSocket() {
        try {
            if (username != null && !username.isEmpty()) {
                CharadesGameServer.removeUser(username);
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}