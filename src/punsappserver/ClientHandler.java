package punsappserver;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

import static punsappserver.CharadesGameServer.getRandomWord;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private ServerListener serverListener;
    private boolean countdownStarted = false;
    private String username;

    public ClientHandler(Socket clientSocket, ServerListener serverListener) {
        this.clientSocket = clientSocket;
        this.serverListener = serverListener;
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
                    serverListener.onClearCanvasReceived(messageServer);
                } else if (Objects.equals(message.getMessageType(), "START") && !countdownStarted) {
                    handleMessage(messageServer);
                    countdownStarted = true;
                    CharadesGameServer.onCountdownStartReceived();
                } else if (Objects.equals(message.getMessageType(), "SET_USERNAME")) {
                    username = message.getUsername();
                    CharadesGameServer.addUser(username, clientSocket);

                } else if (Objects.equals(message.getMessageType(), "COLOR_CHANGE")) {
                    serverListener.onColorReceived(messageServer);
                }
                else if (Objects.equals(message.getMessageType(), "CHAT")){
                        String randomWord = getRandomWord();
                        String chatMessage = message.getChat().trim().toLowerCase();
                        if (chatMessage.equals(randomWord.toLowerCase())) {
                            Message guessedWordMessage = new Message();
                            guessedWordMessage.setMessageType("CHAT");
                            guessedWordMessage.setChat(username + " guessed the word! - " + randomWord);
                            String winMessage = new Gson().toJson(guessedWordMessage);
                            handleMessage(winMessage);
                        }
                        else{
                            handleMessage(messageServer);
                        }
                }
                else {
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
        serverListener.onMessageReceived(message);
    }

    private void closeClientSocket() {
        try {
            CharadesGameServer.removeUser(username);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}