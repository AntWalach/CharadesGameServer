package punsappserver;

public interface ServerListener {
    void onChatMessageReceived(String message);
    void onCoordinatesReceived(double x, double y);
    void onClearCanvasReceived();

}
