package sample;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class VideoStreaming {

    public static String URL = "rtsp://192.168.1.1/live/ch00_1";
    private static VideoCapture capture;
    private static Mat frame = null;
    private static boolean streamingStarted;

    public static void StartStreaming() {
        streamingStarted = false;
        capture = new VideoCapture(URL);
        frame = new Mat();
        if (!capture.isOpened()) {
            System.out.println("Camera not found!");
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (capture.isOpened()) {
                    capture.read(frame);
                }
            }
        });
        thread.start();
    }


    public static Mat getFrame() {
        return frame;
    }

    public static VideoCapture getCapture() {
        return capture;
    }

    public static boolean isStreamingStarted() {
        return streamingStarted;
    }

    public static BufferedImage Mat2BufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }


}
