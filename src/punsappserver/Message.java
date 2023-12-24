package punsappserver;

public class Message {
    // Attributes of the Message class representing different message properties
    int roomId; // Room ID associated with the message
    String username; // Username related to the message
    String messageType; // Type of message (e.g., "CHAT", "TURN_INFO", "WORD_INFO")
    String chat; // Content of the message (e.g., chat text, information)
    double x; // X-coordinate
    double y; // Y-coordinate

    // Additional attribute specific to certain message types
    private String color; // Color information for the message (if applicable)

    // Default constructor initializing message attributes
    public Message() {
        this.roomId = -1; // Default room ID set to -1
        this.username = ""; // Default username set to an empty string
        this.messageType = ""; // Default message type set to an empty string
        this.chat = ""; // Default chat content set to an empty string
        this.x = 0; // Default X-coordinate set to 0
        this.y = 0; // Default Y-coordinate set to 0
    }

    // Getters and setters for the Message attributes
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
