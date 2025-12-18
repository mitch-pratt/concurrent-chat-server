import javax.sound.sampled.*;
import java.net.*;

public class VoIPServer implements Runnable {

    private int port;
    private static final int BUFFER_SIZE = 1024;  // Audio buffer size

    public VoIPServer(int port) {
        this.port = port;
    }

    @Override
    public void run(){
        try {
            // Set up audio format
            AudioFormat format = new AudioFormat(8000, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            // Get the speakers line
            SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();

            // Create a DatagramSocket to listen for incoming UDP packets
            DatagramSocket socket = new DatagramSocket(port);

            // Buffer to receive incoming audio packets
            byte[] buffer = new byte[BUFFER_SIZE];

            System.out.println("Server listening on port " + port);

            while (true) {

                if(Thread.currentThread().isInterrupted()) {
                    break;
                }

                // Receive the audio data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Play received audio data through speakers
                speakers.write(packet.getData(), 0, packet.getLength());
            }
            speakers.stop();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
