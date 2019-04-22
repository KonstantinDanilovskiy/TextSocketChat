package support;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Thread прерван.");
        }
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        synchronized (in) {
            return (Message) in.readObject();
        }
    }

    @Override
    public void close() {
        try {
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Произошла ошибка при закрытии потоков: " + e);
        }

    }
}
