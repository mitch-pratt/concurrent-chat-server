import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

public class CameraStreaming {

        public CameraStreaming() {
            // Load the OpenCV library
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            // Open the default camera (0)
            VideoCapture camera = new VideoCapture(0);

            if (!camera.isOpened()) {
                System.out.println("Error: Could not open webcam.");
                return;
            }

            // Create a Mat object to store the captured frame
            Mat frame = new Mat();

            // Loop to continuously capture and display frames
            while (true) {
                // Capture a new frame from the webcam
                camera.read(frame);

                // Check if frame is empty
                if (frame.empty()) {
                    System.out.println("Error: Blank frame captured.");
                    break;
                }

                // Convert the frame to grayscale (optional)
                //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);

                // Display the frame in a window
                HighGui.imshow("Webcam", frame);

                // Wait for key press and exit if 'ESC' is pressed
                if (HighGui.waitKey(1) == 27) {
                    break;
                }
            }

            // Release the camera and close any OpenCV windows
            camera.release();
            HighGui.destroyAllWindows();
        }
    }


