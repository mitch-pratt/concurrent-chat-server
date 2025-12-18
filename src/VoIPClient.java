import javax.sound.sampled.*;
import java.net.*;
import java.io.*;

public class VoIPClient implements Runnable {

    private String host;
    private int port;
    private static final int BUFFER_SIZE = 1024;  // Audio buffer size

    public VoIPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run(){
        try {
            // Set up audio format
            AudioFormat format = new AudioFormat(8000, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // Get the microphone line
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            // Create a DatagramSocket to send/receive UDP packets
            DatagramSocket socket = new DatagramSocket();

            // Buffer for storing audio data
            byte[] buffer = new byte[BUFFER_SIZE];

            // Infinite loop to continuously capture and send audio
            while (true) {

                if(Thread.currentThread().isInterrupted()) {
                    break;
                }

                // Capture audio into the buffer
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                if (bytesRead > 0) {
                    // Send captured audio data as a UDP packet
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, InetAddress.getByName(host), port);
                    socket.send(packet);
                }
            }
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
