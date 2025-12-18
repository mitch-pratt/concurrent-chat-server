import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class client extends Thread {

    String host;
    int port;
    String username;

    ClientGUI clientGUI;

    Socket echoSocket;
    DataInputStream dataIn;
    DataOutputStream dataOut;
    private volatile boolean running = true;

    public client(String host, int port, String username, ClientGUI clientGUI) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.clientGUI = clientGUI;

        try {
            echoSocket = new Socket(host, port);
            dataIn = new DataInputStream(echoSocket.getInputStream());
            dataOut = new DataOutputStream(echoSocket.getOutputStream());
        } catch (ArrayIndexOutOfBoundsException a) {
            System.out.println(a);
        }
        catch (Exception e) {
            try {
                echoSocket.close();
                dataIn.close();
                dataOut.close();
            } catch (IOException ex) {
                //warning printing Exception. Just needed something in the catch.
                System.out.println(ex);
            }
        }

    }

    //Main, makes thread to look for messages and if a message is sent output it on the screen
    //Main thread reads what the user types and sends it to everyone
//     public static void main(String[] args) {
//        client client = new client(args[0], Integer.parseInt(args[1]), args[2]);
//
//        //listening for messages
//        Thread thread = new Thread(client);
//        thread.start();
//
//        client.getMessage();
//
//    }

    //get Message from user and sends to client handler to send to everyone else
    public void getMessage(String message) {
        try {
            // Command 0 for text message
            dataOut.write(0);
            byte[] messageBytes = message.getBytes("UTF-8");
            dataOut.writeShort(messageBytes.length);
            dataOut.write(messageBytes);
            dataOut.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("Client listener started");
        //use command var to switch between listening for files and text bc the code / reading is different
        //command is set when text / file is sent to other client
        //read from dataIn
        try {
            while (running) {
                int command = dataIn.read();
                if (command == -1) break;

                switch (command) {
                    case 0: // Text message
                        handleTextMessage();
                        break;
                    case 1: // File data
                        receiveFileData();
                        break;
                    case 2: // File not found
                        System.out.println("File not found on server");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Connection lost: " + e.getMessage());
        }
    }

    private void handleTextMessage() throws IOException {
        int messageLength = dataIn.readUnsignedShort();
        byte[] messageBytes = new byte[messageLength];
        dataIn.readFully(messageBytes);
        String message = new String(messageBytes, "UTF-8");
        if(clientGUI == null){
            System.out.println(message);
        }
        else {
            clientGUI.new_message_in(message);
        }
    }

    private void receiveFileData() {
        try {
            long fileSize = dataIn.readLong();
            int fileNameLength = dataIn.readUnsignedShort();
            byte[] fileNameBytes = new byte[fileNameLength];
            dataIn.readFully(fileNameBytes);
            String fileName = new String(fileNameBytes, "UTF-8");

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
                saveFile(fileName, fileData);
            }
        } catch (IOException e) {
            System.err.println("Error receiving file: " + e.getMessage());
        }
    }

    private void saveFile(String fileName, byte[] fileData) {
        String userHome = System.getProperty("user.home");
        String downloadsFolderPath = userHome + File.separator + "Downloads" + File.separator + fileName;

        try {
            File file = new File(downloadsFolderPath);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
            }

            ImageIcon icon = new ImageIcon(downloadsFolderPath);
            JOptionPane.showMessageDialog(null, "File downloaded successfully: " + fileName + " to " + downloadsFolderPath);
            JOptionPane.showMessageDialog(null, icon);

        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error saving file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void receiveFile(String fileName) {
        try {

            dataOut.write(3);
            byte[] fileNameBytes = fileName.getBytes("UTF-8");
            dataOut.writeShort(fileNameBytes.length);
            dataOut.write(fileNameBytes);
            dataOut.flush();
            System.out.println("Requesting file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error requesting file: " + e.getMessage());
        }
    }

    public void sendFile(File file) {
        try {
            String fileName = file.getName();
            long fileSize = file.length();


            dataOut.write(1);
            dataOut.writeLong(fileSize);

            byte[] fileNameBytes = fileName.getBytes("UTF-8");
            dataOut.writeShort(fileNameBytes.length);
            dataOut.write(fileNameBytes);

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, bytesRead);
                }
            }
            dataOut.flush();


            getMessage("File available: " + fileName);
            System.out.println("File sent: " + fileName);
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
        }
    }


}
