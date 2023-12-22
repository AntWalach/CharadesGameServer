package punsappserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomServer {
    ServerSocket roomServerSocket;
    private final int roomPort;
    static final List<Socket> roomClientSockets = new CopyOnWriteArrayList<>();

    public RoomServer(int roomPort) throws IOException {
        this.roomPort = roomPort;
    }

    public void run() {
        try {
            roomServerSocket = new ServerSocket(roomPort);
            System.out.println("Room server running on port " + roomPort);
            // Obsługa klientów na nowym porcie
            while (true) {
                Socket clientSocket = roomServerSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Adding the client to the list of clients
                roomClientSockets.add(clientSocket);

                // Creating a thread to handle the client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
