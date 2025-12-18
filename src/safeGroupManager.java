import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class safeGroupManager implements groupManager {
    private final Map<String, List<clientHandler>> groupChats = new HashMap<>();
    private final Object lock = new Object();

    //send message so know its sending clients
    //send to client using out

    @Override
    public void joinChat(String chatName, clientHandler client) {
        synchronized (lock) {

            leaveChat(client.getCurrentChat(), client);

            // Ensure the new chat exists
            groupChats.putIfAbsent(chatName, new CopyOnWriteArrayList<>());

            // Add client to the new chat
            groupChats.get(chatName).add(client);
            client.setCurrentChat(chatName);

            System.out.println("Client " + client + " joined chat: " + chatName);
            System.out.println("Clients in " + chatName + ": " + groupChats.get(chatName).size());
            
            System.out.println("You have joined the " + chatName + " chat.");
        }
    }




    @Override
    public List<String> getGroupChats() {
        synchronized (lock) {
            return new ArrayList<>(groupChats.keySet()); // Return list of chat names
        }
    }

    
    @Override
    public void leaveChat(String chatName, clientHandler client) {
        synchronized (lock) {
            List<clientHandler> clients = groupChats.get(chatName);
            if (clients != null) {
                clients.remove(client);
                System.out.println("Client " + client + " left chat: " + chatName);
            }
        }
    }

    @Override
    public void sendMessage(String chatName, String message, clientHandler sender) {
        synchronized (lock) {
            List<clientHandler> clients = groupChats.getOrDefault(chatName, new ArrayList<>());

            System.out.println("Sending message to " + chatName + " (" + clients.size() + " clients): " + message);

            for (clientHandler client : clients) {
                if (client != sender) {
                    try {
                        byte[] messageBytes = message.getBytes("UTF-8");
                        client.getDataOutputStream().write(0); // Text message command
                        client.getDataOutputStream().writeShort(messageBytes.length);
                        client.getDataOutputStream().write(messageBytes);
                        client.getDataOutputStream().flush();
                    } catch (IOException e) {
                        System.err.println("Error sending message to client in group " + chatName + ": " + e.getMessage());
                    }
                }
            }
        }
    }


    @Override
    public List<clientHandler> getClientsInChat(String room) {
        synchronized (lock) {
            return groupChats.getOrDefault(room, Collections.emptyList());
        }
    }

}
