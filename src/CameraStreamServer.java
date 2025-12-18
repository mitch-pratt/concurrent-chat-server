import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class CameraStreamServer implements Runnable {

    private int port;
    public CameraStreamServer(int port){
        this.port = port;
    }

    @Override
    public void run() {

    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    VoIPServer voipServer = new VoIPServer(port + 3000);
    Thread thread = new Thread(voipServer);
    thread.start();


        try{
            DatagramSocket socket = new DatagramSocket(port);
            byte[] buffer = new byte[100000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            JFrame jframe = null;
            JPanel panel = new JPanel();

            final boolean[] running = {true};
            while(running[0]){

                socket.receive(packet);
                //System.out.println("received");
                byte[] buff = packet.getData();
                ByteArrayInputStream bain = new ByteArrayInputStream(buff);

                BufferedImage image = ImageIO.read(bain);

                if(jframe == null){
                    jframe = new JFrame();
                    jframe.setTitle("stained_image");
                    jframe.setSize(720, 480);
                    jframe.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            running[0] = false;
                            thread.interrupt();
                        }
                    });
                    jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    panel.setSize(720, 480);
                    JLabel label=new JLabel();
                    label.setIcon(new ImageIcon(image));
                    panel.add(label, BorderLayout.CENTER);
                    jframe.add(panel);
                    jframe.setLocationRelativeTo(null);
                    jframe.pack();
                    jframe.setVisible(true);
                }
                else {
                    panel.removeAll();
                    panel.revalidate();
                    JLabel label = new JLabel();
                    label.setIcon(new ImageIcon(image));
                    panel.add(label, BorderLayout.CENTER);
                    panel.repaint();
                    jframe.revalidate();
                    jframe.repaint();
                }

            }
            socket.close();
        }
        catch (Exception e) {
            System.out.println(e);

        }


    }
    public Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

}
