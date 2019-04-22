package client;

public class ClientGuiController extends Client {
    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);

    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    @Override
    public void run() {
        getSocketThread().run();
    }

    @Override
    public String getServerAddress() {
        return view.getServerAddress();
    }

    @Override
    public int getServerPort() {
        return view.getServerPort();
    }

    @Override
    public String getUserName() {
        setName(view.getUserName());
        return getName();
    }

    public ClientGuiModel getModel() {
        return model;
    }


    public class GuiSocketThread extends SocketThread {
        @Override
        public void processIncomingMessage(String message) {
            model.setNewMessage(message);
            view.refreshMessages();
        }

        @Override
        public void informAboutAddingNewUser(String data) {
            String userName;
            if (data.contains("Date:"))  {
                userName = data.substring(0, data.indexOf("Date:"));
                String connectionDate = data.substring(data.indexOf("Date:") + 5, data.length());
                model.setNewMessage("Пользователь " + userName + " в чате. Дата подключения: " + connectionDate);
            }
            else
            {
                userName = data;
                model.setNewMessage("Пользователь " + data + " подключился к чату.");
            }
            view.refreshMessages();
            model.addUser(userName);
            view.refreshUsers();
        }

        @Override
        public void informAboutDeletingNewUser(String userName) {
            model.setNewMessage("Пользователь " + userName + " покинул чат.");
            view.refreshMessages();
            model.deleteUser(userName);
            view.refreshUsers();
        }

        @Override
        public void notifyConnectionStatusChanged(boolean clientConnected) {
            view.notifyConnectionStatusChanged(clientConnected);
        }

        @Override
        protected void showUser() {
            view.setUserName();
        }
    }

    public static void main(String[] args) {
        new ClientGuiController().run();
    }
}
