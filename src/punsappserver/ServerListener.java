package punsappserver;

import java.net.Socket;

public interface ServerListener {
    void onMessageReceived(String message);

    void onClearCanvasReceived(String messageServer);

    void onColorReceived(String messageServer);
}
