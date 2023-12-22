package punsappserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class BroadcastRoom {
    static void broadcastRoom(String message) {
        for (Socket socket : RoomServer.roomClientSockets) {
            try {
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                socketOut.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
