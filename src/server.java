import java.net.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class server {
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        int portNumber = Integer.parseInt(args[0]);
        System.out.println("Starting server on port " + portNumber);

        //LocalDateTime myDateObj = LocalDateTime.now();
        //System.out.println("Before formatting: " + myDateObj);
        //DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        //String formattedDate = myDateObj.format(myFormatObj);

        try{
            File logfile = new File("log.txt");
            if (logfile.createNewFile()) {
                System.out.println("File created: " + logfile.getName());
            } else {
                System.out.println("File already exists.");
            }
            //FileWriter myWriter = new FileWriter("log.txt", true);
            //myWriter.write("Chat opened " + formattedDate + "\n");
            //myWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


        safeGroupManager groupManager = new safeGroupManager();
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server is running on port " + portNumber);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                clientHandler handler = new clientHandler(clientSocket, groupManager);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}
