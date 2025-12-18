import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class clientHandler implements Runnable {

    public static final File log_file = new File("log.txt");

    public static ReentrantReadWriteLock logReadWriteLock = new ReentrantReadWriteLock();
    public static final CopyOnWriteArrayList<clientHandler> clients = new CopyOnWriteArrayList<>();
    private final Socket socket;
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;
    private final safeGroupManager groupManager;
    private final Lock currentChatLock = new ReentrantLock();
    protected volatile String currentChat = "global";

    public static final CopyOnWriteArrayList<String> pinnedMessages = new CopyOnWriteArrayList<>();

    protected static class PendingFile {
        final String fileName;
        final byte[] data;
        final long size;

        PendingFile(String fileName, byte[] data, long size) {
            this.fileName = fileName;
            this.data = data;
            this.size = size;
        }
    }

    private static final CopyOnWriteArrayList<PendingFile> pendingFiles = new CopyOnWriteArrayList<>();
    private final Lock pendingFilesLock = new ReentrantLock();

    public clientHandler(Socket socket, safeGroupManager groupManager) throws IOException {
        this.socket = socket;
        this.groupManager = groupManager;
        this.dataIn = new DataInputStream(socket.getInputStream());
        this.dataOut = new DataOutputStream(socket.getOutputStream());
        clients.add(this);
        this.groupManager.joinChat("global", this);
    }

    public clientHandler() throws IOException {
        this.socket = null;
        this.dataIn = null;
        this.dataOut = null;
        this.groupManager = null;
    }


    @Override
    public void run() {
        System.out.println("ClientHandler started");
        try {
            while (!Thread.interrupted()) {
                int command = dataIn.read();
                if (command == -1) break;

                switch (command) {
                    case 0: // Text message
                        handleTextMessage();
                        break;
                    case 1: // File upload
                        receiveFile();
                        break;
                    case 3: // File request
                        handleFileRequest();
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Connection lost: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleTextMessage() throws IOException {
        int messageLength = dataIn.readUnsignedShort();
        byte[] messageBytes = new byte[messageLength];
        dataIn.readFully(messageBytes);
        String message = new String(messageBytes, "UTF-8");

        if (message.startsWith("PIN:")) {
            // Extract the original message without the PIN: prefix
            pinMessage(message.substring("PIN:".length()));
        } else if (message.equals("SHOW_PINNED")) {
            sendPinnedList();
        } else if (message.startsWith("DISABLE_PIN:")) {
            // Forward the disable pin message to all clients
            sendToAll(message, true); // System message should go to all
        } else if(message.equals("/log")) {
            sendLog();
        } else if (message.equals("/list")) {
            List<String> groupNames = this.groupManager.getGroupChats();
            // Send only to the requesting client
            sendToClient(this, "Available group chats: " + String.join(", ", groupNames));
        } else if (message.startsWith("/join ")) {
            String groupName = message.substring(6).trim(); // Remove "/join " prefix
            // Remove surrounding quotes if present
            if (groupName.startsWith("\"") && groupName.endsWith("\"")) {
                groupName = groupName.substring(1, groupName.length() - 1);
            }
            System.out.println("Client requested to join: " + groupName);
            this.groupManager.joinChat(groupName, this);
            this.currentChat = groupName;
            // Send join notification only to members of the new group
            this.groupManager.sendMessage(groupName, "User joined chat: " + groupName, this);
        } else if (message.startsWith("/leave")) {
            String oldChat = this.currentChat;
            // First notify the current group that user is leaving
            this.groupManager.sendMessage(oldChat, "User left chat: " + oldChat, this);
            // Then return to global
            returnToGlobal();
        } else {
            if (this.currentChat.equals("global")) {
                sendToAll(message, false);
            } else {
                this.groupManager.sendMessage(this.currentChat, message, this);
            }
        }
    }

    private void sendToClient(clientHandler client, String message) {
        try {
            byte[] messageBytes = message.getBytes("UTF-8");
            client.dataOut.write(0); // Text message command
            client.dataOut.writeShort(messageBytes.length);
            client.dataOut.write(messageBytes);
            client.dataOut.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
        }
    }

    protected void sendToAll(String message, boolean isSystemMessage) {

        if(!isSystemMessage) {
            addToLog(message);
        }

        synchronized (clients) {

            byte[] messageBytes;
            try {
                messageBytes = message.getBytes("UTF-8");
                for (clientHandler client : clients) {
                    if (!client.equals(this) && (isSystemMessage || client.currentChat.equals(this.currentChat))) {
                        try {
                            client.dataOut.write(0); // Text message command
                            client.dataOut.writeShort(messageBytes.length);
                            client.dataOut.write(messageBytes);
                            client.dataOut.flush();
                        } catch (IOException e) {
                            System.err.println("Error sending message to client: " + e.getMessage());
                        }
                    }
                }

            }
            catch (Exception e) {
                System.err.println("Error encoding message: " + e.getMessage());
            }
        }

    }

    private void returnToGlobal() {
        if (!this.currentChat.equals("global")) {
            this.groupManager.leaveChat(this.currentChat, this);
            this.groupManager.joinChat("global", this);
            this.currentChat = "global";
            try {
                // Send the global chat notification to all clients
                for (clientHandler client : clients) {
                    sendToClient(client, "User returned to global chat");
                }
            } catch (Exception e) {
                System.err.println("Error sending return to global message: " + e.getMessage());
            }
        }
    }

    private void receiveFile() throws IOException {
        long fileSize = dataIn.readLong();
        String fileName = readString();
        System.out.println("Receiving file: " + fileName + " (Size: " + fileSize + " bytes)");

        byte[] fileData = new byte[(int)fileSize];
        int totalReceived = 0;
        while (totalReceived < fileSize) {
            int bytesToRead = (int)Math.min(8192, fileSize - totalReceived);
            int bytesRead = dataIn.read(fileData, totalReceived, bytesToRead);
            if (bytesRead == -1) break;
            totalReceived += bytesRead;
        }

        if (totalReceived == fileSize) {
            addPendingFile(fileName, fileData, fileSize);
            System.out.println("File received and stored: " + fileName);
        }
    }

    private void handleFileRequest() throws IOException {
        String fileName = readString();
        PendingFile pendingFile = findPendingFile(fileName);

        if (pendingFile != null) {
            sendFile(pendingFile);
        } else {
            dataOut.write(2); // File not found
            dataOut.flush();
        }
    }

    private void sendFile(PendingFile file) throws IOException {
        dataOut.write(1); // File found
        dataOut.writeLong(file.size);
        writeString(file.fileName);
        dataOut.write(file.data);
        dataOut.flush();
    }

    private String readString() throws IOException {
        int length = dataIn.readUnsignedShort();
        byte[] bytes = new byte[length];
        dataIn.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    private void writeString(String str) throws IOException {
        byte[] bytes = str.getBytes("UTF-8");
        dataOut.writeShort(bytes.length);
        dataOut.write(bytes);
    }

    protected void cleanup() {
        try {
            clients.remove(this);
            socket.close();
            System.out.println("Client disconnected, remaining clients: " + clients.size());
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    private void sendPinnedList() throws IOException {
        StringBuilder pinnedList = new StringBuilder("PINNED_LIST:");
        for (int i = 0; i < pinnedMessages.size(); i++) {
            pinnedList.append(pinnedMessages.get(i));
            if (i < pinnedMessages.size() - 1) {
                pinnedList.append("|");
            }
        }
        //make sure is text
        byte[] responseBytes = pinnedList.toString().getBytes("UTF-8");
        dataOut.write(0); // Text message command
        dataOut.writeShort(responseBytes.length);
        dataOut.write(responseBytes);
        dataOut.flush();
    }

    private void sendLog() throws IOException {
        StringBuilder logList = new StringBuilder("LOG_LIST:");

        logReadWriteLock.readLock().lock();
        Scanner scanner = new Scanner(log_file);
        while (scanner.hasNextLine()) {
            logList.append(scanner.nextLine());
            if (scanner.hasNextLine()) {
                logList.append("|");
            }
        }
        logReadWriteLock.readLock().unlock();

        //make sure is text
        byte[] responseBytes = logList.toString().getBytes("UTF-8");
        dataOut.write(0); // Text message command
        dataOut.writeShort(responseBytes.length);
        dataOut.write(responseBytes);
        dataOut.flush();
    }
    public synchronized void pinMessage(String message) {
        pinnedMessages.add(message);
        sendToAll("Pinned: " + message, true);
    }

    protected void addPendingFile(String fileName, byte[] data, long size) {
        pendingFiles.add(new PendingFile(fileName, data, size));
    }

    protected PendingFile findPendingFile(String fileName) {
        return pendingFiles.stream()
                .filter(pf -> pf.fileName.equals(fileName))
                .findFirst()
                .orElse(null);
    }

    public String getCurrentChat() {
        return this.currentChat;
    }

    public void setCurrentChat(String newChat) {
        this.currentChat = newChat;
    }

    public DataOutputStream getDataOutputStream() {
        return this.dataOut;
    }

    public void addToLog(String message) {

        logReadWriteLock.writeLock().lock();
        try{
            FileWriter myWriter = new FileWriter("log.txt", true);
            myWriter.write(message + "\n");
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logReadWriteLock.writeLock().unlock();

    }

    public void addToLogTEST(String message) {
        logReadWriteLock.writeLock().lock();
        try{
            FileWriter myWriter = new FileWriter(log_file, true);
            for(int i=0;i<10;i++){
                myWriter.write(message + "\n");
                myWriter.write("message info" + "\n");
            }
            myWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logReadWriteLock.writeLock().unlock();
    }
}

