package punsappserver;

import java.util.LinkedList;
import java.util.Queue;
import java.net.ServerSocket;
import java.net.Socket;

public class GameQueue {
    private Queue<Socket> queue;
    public GameQueue() {
        queue = new LinkedList<>();
    }

    public synchronized void enqueue(Socket clientSocket) {
        if (queue.size() < 4) {
            queue.add(clientSocket);
        }
    }

    public synchronized Socket dequeue() {
        return queue.poll();
    }

    public synchronized int queueSize() {
        return queue.size();
    }
}
