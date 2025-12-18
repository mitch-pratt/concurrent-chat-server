import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CameraStreamClient implements Runnable {

    private int port;
    private String host;
    public CameraStreamClient(int port, String host) {
        this.port = port;
        this.host = host;
    }

    @Override
    public void run(){

        VoIPClient voIPClient = new VoIPClient(host, port+3000);
        Thread thread = new Thread(voIPClient);
        thread.start();

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        final boolean[] running = {true};


        JFrame jframe = new JFrame();
        JPanel panel = new JPanel();
        JButton stopButton = new JButton("Stop");
        panel.add(stopButton);
        jframe.add(panel);
        jframe.pack();
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        stopButton.addActionListener(e -> {
            running[0] = false;
            thread.interrupt();
        });

        try{

            DatagramSocket socket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(host);
            byte[] buffer;

            VideoCapture camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                System.out.println("Can't open capture");
                return;
            }
            Mat frame = new Mat();

            while(running[0]) {
                camera.read(frame);
                if(frame.empty()) {
                    System.out.println("Frame is empty");
                    return;
                }

                Imgproc.resize(frame, frame, new Size(720, 480));

                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".jpg", frame, matOfByte);
                byte[] byteArray = matOfByte.toArray();
                //Preparing the Buffered Image
                InputStream in = new ByteArrayInputStream(byteArray);
                BufferedImage image = ImageIO.read(in);

                //sending the image
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                baos.flush();
                byte[] bytes = baos.toByteArray();
                //System.out.println(bytes.length);
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, ip, port);
                socket.send(packet);


            }
            socket.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }

    }
}
