package client;

import support.Connection;
import support.ConsoleHelper;
import support.Message;
import support.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;
    private String name;


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера (ip или localhost): ");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера: ");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите ваше имя: ");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("ClientError " + e);
            clientConnected = false;
        }
    }

    public void run() {
        Thread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch(InterruptedException e) {
            ConsoleHelper.writeMessage("Interrupted exception occurred: " + e);
        }
        if (clientConnected == true) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        }
        else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        String text;
        while (clientConnected) {
            text = ConsoleHelper.readString();
            if (text.equals("exit")) break;
            if (shouldSendTextFromConsole()) sendTextMessage(text);
        }

    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String data) {
            if (data.contains("Date:"))  {
                ConsoleHelper.writeMessage("Пользователь " + data.substring(0, data.indexOf("Date:")) + " в чате. Дата подключения: " + data.substring(data.indexOf("Date:") + 5, data.length()));
            }
            else
            {
                ConsoleHelper.writeMessage("Пользователь " + data + " подключился к чату.");
            }
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("Пользователь " + userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void showUser() {
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            String name;
            while (true) {
                Message message = connection.receive();

// Вариант кода с использованием оператора switch вместо if
//                switch (message.getType()) {
//                    case NAME_REQUEST:
//                        name = getUserName();
//                        connection.send(new support.Message(support.MessageType.USER_NAME, name));
//                        break;
//                    case NAME_ACCEPTED:
//                        notifyConnectionStatusChanged(true);
//                        return;
//                    default:
//                        throw new IOException("Unexpected support.MessageType");
//                }

                if (message.getType() == MessageType.NAME_REQUEST) {
                    name = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, name));
                }
                else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                }
                else throw new IOException("Unexpected support.MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while(true) {
                Message message = connection.receive();

// Вариант кода с использованием оператора switch вместо if
//                switch (message.getType()) {
//                    case TEXT:
//                        processIncomingMessage(message.getData());
//                        break;
//                    case USER_ADDED:
//                        informAboutAddingNewUser(message.getData());
//                        break;
//                    case USER_REMOVED:
//                        informAboutDeletingNewUser(message.getData());
//                        break;
//                    default:
//                        throw new IOException("Unexpected support.MessageType");
//                }

                if (message.getType() == MessageType.TEXT) processIncomingMessage(message.getData());
                else if (message.getType() == MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
                else if (message.getType() == MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
                else throw new IOException("Unexpected support.MessageType");
            }
        }

        public void run() {
            try {
                String serverAddress = getServerAddress();
                int port = getServerPort();
                Socket socket = new Socket(serverAddress, port);
                Client.this.connection = new Connection(socket);
                clientHandshake();
                showUser();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            } finally {
                if (Client.this.connection != null) {
                    Client.this.connection.close();
                }
            }
        }
    }


    public static void main(String[] args) {
        Client client = new Client();
        client.run();

    }
}