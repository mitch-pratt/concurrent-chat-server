//video playback needs doing -- embedding videos javacv library is the one to use
// javafx -- loibrary

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class unsafeGroupManager implements groupManager{

    private final Map<String, List<clientHandler>> groupChats = new HashMap<>();

    @Override
    public void joinChat(String chatName, clientHandler client) {
        groupChats.putIfAbsent(chatName, new ArrayList<>());
        groupChats.get(chatName).add(client);


        System.out.println("Client " + client + " joined chat: " + chatName);
        System.out.println("Clients in " + chatName + ": " + groupChats.get(chatName).size());
    }
    
    @Override
    public void leaveChat(String chatName, clientHandler client) {
        groupChats.getOrDefault(chatName, new ArrayList<>()).remove(client);
    }

    @Override
    public void sendMessage(String chatName, String message, clientHandler sender) {
        for (clientHandler client : groupChats.getOrDefault(chatName, new ArrayList<>())) {
            if (client != sender) {
                System.out.println(message);
            }
        }
    }

    @Override
    public List<String> getGroupChats() {
        return new ArrayList<>(groupChats.keySet()); // Return list of chat names
    }

    @Override
    public List<clientHandler> getClientsInChat(String room) {
            return groupChats.getOrDefault(room, Collections.emptyList());
    }

}
