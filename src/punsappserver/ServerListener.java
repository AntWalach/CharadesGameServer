package punsappserver;

public interface ServerListener {
    void onMessageReceived(String message);
    void onCoordinatesReceived(double x, double y);
    void onClearCanvasReceived();

}
