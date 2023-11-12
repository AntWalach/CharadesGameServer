import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message: " + message);

                // Przekaż wiadomość od jednego klienta do innych klientów poprzez serwer
                ChatServer.broadcastMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Usuń klienta z listy klientów i zamknij połączenie
            ChatServer.clientSockets.remove(clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
