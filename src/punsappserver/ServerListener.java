package punsappserver;

public interface ServerListener {
    void onMessageReceived(String message);
    void onClearCanvasReceived(String message);

}
