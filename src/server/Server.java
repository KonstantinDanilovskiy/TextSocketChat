package server;


import support.Connection;
import support.ConsoleHelper;
import support.Message;
import support.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private static Map<String, String> connectionDateMap = new ConcurrentHashMap<>();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM в HH:mm");


    private static class Handler extends Thread {
        Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            connection.send(new Message(MessageType.NAME_REQUEST, "What is your name?"));
            Message message = connection.receive();
            String data = message.getData();
            if (message.getType() == MessageType.USER_NAME && !data.isEmpty() && !connectionMap.containsKey(data)) {
                connectionMap.put(data, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED, "Your name is accepted."));
            }
            else {
                return serverHandshake(connection);
            }
            return data;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String name : connectionMap.keySet()) {
                if (!name.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, name + "Date:" + connectionDateMap.get(name)));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    Message newMessage = new Message(MessageType.TEXT, userName + ": " + message.getData());
                    sendBroadcastMessage(newMessage);
                }
                else {
                    ConsoleHelper.writeMessage("Полученное сообщение не является текстовым.");
                }
            }
        }

        public void run() {
            ConsoleHelper.writeMessage("Установлено соединение с удаленным клиентом " + socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                Date date = new GregorianCalendar().getTime();
                String stringDate = dateFormat.format(date);
                connectionDateMap.put(userName, stringDate);
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);


            } catch(ClassNotFoundException e2) {
                ConsoleHelper.writeMessage("Ошибка соединения " + e2);
            } catch(IOException e1) {
                ConsoleHelper.writeMessage("Ошибка соединения " + e1);
            } finally {
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }

        }
    }


    public static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((name, connect) -> { try {
            connect.send(message);
        } catch(IOException e) {
            ConsoleHelper.writeMessage("Сообщение не отправлено. " + e);}
        });

    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите номер порта для socket соединения:");
        int port = ConsoleHelper.readInt();
        try ( ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Сервер запущен.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Handler(clientSocket).start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}

