import org.opencv.core.Core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;




public class Main {
    public static void main(String[] args) throws IOException {



        System.out.println("Hello world!");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ClientGUI clientGUI = new ClientGUI();
    }
}