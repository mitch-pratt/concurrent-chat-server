import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class unsafeClientHandler extends clientHandler {
    // Override the static collections to use unsafe versions
    public static ArrayList<clientHandler> clients = new ArrayList<>();
    public static ArrayList<String> pinnedMessages = new ArrayList<>();
    private static ArrayList<PendingFile> pendingFiles = new ArrayList<>();
    public static File log_file = new File("log.txt");

    public unsafeClientHandler(Socket socket, safeGroupManager groupManager) throws IOException {
        super(socket, groupManager);
    }
    public unsafeClientHandler() throws IOException {
       super();
    }

    @Override
    public void pinMessage(String message) {
        pinnedMessages.add(message);
        sendToAll("Pinned: " + message, true);
    }

    @Override
    public String getCurrentChat() {
        return currentChat;
    }

    @Override
    public void setCurrentChat(String newChat) {
        currentChat = newChat;
    }

    @Override
    protected void sendToAll(String message, boolean isSystemMessage) {
        byte[] messageBytes;
        try {
            messageBytes = message.getBytes("UTF-8");
            for (clientHandler client : clients) {
                if (!client.equals(this) && (isSystemMessage || client.currentChat.equals(this.currentChat))) {
                    try {
                        client.getDataOutputStream().write(0); // Text message command
                        client.getDataOutputStream().writeShort(messageBytes.length);
                        client.getDataOutputStream().write(messageBytes);
                        client.getDataOutputStream().flush();
                    } catch (IOException e) {
                        System.err.println("Error sending message to client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error encoding message: " + e.getMessage());
        }
    }

    @Override
    protected void cleanup() {
        try {
            clients.remove(this);
            super.cleanup(); // Call parent's cleanup to close socket
            System.out.println("Client disconnected, remaining clients: " + clients.size());
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    @Override
    protected void addPendingFile(String fileName, byte[] data, long size) {
        pendingFiles.add(new PendingFile(fileName, data, size));
    }

    @Override
    protected PendingFile findPendingFile(String fileName) {
        return pendingFiles.stream()
                .filter(pf -> pf.fileName.equals(fileName))
                .findFirst()
                .orElse(null);
    }


    public void addToLog(String message) {

        try{
            FileWriter myWriter = new FileWriter(log_file, true);
            Thread.sleep(new Random().nextInt(100));
            for(int i=0;i<10;i++){
                myWriter.write(message + "\n");
                myWriter.write("message info" + "\n");
            }
            myWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}