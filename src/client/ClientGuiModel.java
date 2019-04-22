package client;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

class ClientGuiModel {
    private final Set<String> allUserNames = new HashSet<>();
    private String newMessage;

    Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);
    }

    String getNewMessage() {
        return newMessage;
    }

    void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    void addUser(String newUserName) {
        allUserNames.add(newUserName);
    }

    void deleteUser(String userName) {
        allUserNames.remove(userName);
    }

}
