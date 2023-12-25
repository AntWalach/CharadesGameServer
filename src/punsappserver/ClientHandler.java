package punsappserver;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

//Handling client messages in waiting room
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private boolean countdownStarted = false;
    private String username;

    // Constructor initializing the client handler with a client socket
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            // Set up an output stream to send messages to the client
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Run method that handles communication with the client
    @Override
    public void run() {
        try {
            // Set up a buffered reader to read incoming messages from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String messageServer;
            // Continuously listen for incoming messages from the client
            while ((messageServer = in.readLine()) != null) {
                System.out.println("Received message: " + messageServer);
                // Deserialize the incoming JSON message into a Message object using Gson
                Gson gson = new Gson();
                Message message = gson.fromJson(messageServer, Message.class);

                // Handle different types of messages from the client
                switch (message.getMessageType()) {
                    case "START":
                        // Initiating a room server based on the received message
                        int roomId = message.getRoomId();
                        int roomPort = 3000 + roomId;
                        handleMessage(messageServer);
                        RoomServer roomServer = new RoomServer(roomPort);
                        new Thread(roomServer).start(); // Start the RoomServer in a new thread
                        break;
                    case "SET_USERNAME":
                        // Assigning the received username to the client's socket
                        username = message.getUsername();
                        CharadesGameServer.addUser(username, clientSocket);
                        break;
                    case "CREATE_ROOM":
                        // Creating a new room in the waiting room management
                        WaitingRoomManagement.createNewRoom();
                        break;
                    case "JOIN_ROOM":
                        int joinedRoomId = message.getRoomId();
                        String joiningUsername = message.getUsername();

                        // Instructing the waiting room management to join a room
                        WaitingRoomManagement.joinRoom(joinedRoomId, joiningUsername);

                        // Broadcast updated player counts to all clients
                        WaitingRoomManagement.countPlayersForEachRoom();
                        break;

                    default:
                        // Forwarding the received message for further handling
                        handleMessage(messageServer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the client socket when the client disconnects
            closeClientSocket();
        }
    }

    // Method to handle the received message
    private void handleMessage(String message) {
        OnMessageReceivedManagement.onMessageReceived(message);
    }

    // Method to close the client socket and remove the associated user
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
