
import java.util.*;

public interface groupManager {
    public List<String> getGroupChats();
    void joinChat(String chatName, clientHandler client);
    void leaveChat(String chatName, clientHandler client);
    void sendMessage(String chatName, String message, clientHandler sender);
    List<clientHandler> getClientsInChat(String room);
}
