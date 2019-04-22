package client;

import support.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client {

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random()*100);
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(": ")) {
                String name = message.substring(0, message.indexOf(":"));
                String text = message.substring(message.indexOf(":") + 2);
                Date date = new GregorianCalendar().getTime();
                if (text.equals("дата"))
                    sendTextMessage(String.format("Информация для %s: %s", name, new SimpleDateFormat("d.MM.YYYY").format(date)));
                if (text.equals("день"))
                    sendTextMessage(String.format("Информация для %s: %s", name, new SimpleDateFormat("d").format(date)));
                if (text.equals("месяц"))
                    sendTextMessage(String.format("Информация для %s: %s", name, new SimpleDateFormat("MMMM").format(date)));
                if (text.equals("год"))
                    sendTextMessage(String.format("Информация для %s: %s", name, new SimpleDateFormat("YYYY").format(date)));
                if (text.equals("время"))
                    sendTextMessage(String.format("Информация для %s: %s", name, new SimpleDateFormat("H:mm:ss").format(date)));
                if (text.equals("час"))
                    sendTextMessage(String.format("Информация для %s: %s", name, new SimpleDateFormat("H").format(date)));
                if (text.equals("минуты"))
                    sendTextMessage(String.format("Информация для %s: %s", name, new SimpleDateFormat("m").format(date)));
                if (text.equals("секунды"))
                    sendTextMessage(String.format("Информация для %s: %s", name, new SimpleDateFormat("s").format(date)));
            }

        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();

        }
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}