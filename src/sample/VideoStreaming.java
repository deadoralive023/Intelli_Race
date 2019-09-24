package sample;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import static sample.ImageManipulation.*;

public class VideoStreaming {

//    public static String URL = "rtsp://192.168.1.1/live/ch00_1";
//    public static String URL = "rtsp://192.168.43.215:8080/h264_ulaw.sdp";
    public static String URL = "rtsp://10.76.218.176:8554/live";
    private static VideoCapture capture;
    private static Mat frame = null;
    public static byte[] finalFrameByteArray;
    public static Point point = new Point(1,1);


    public static void StartStreaming() {
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
//                    finalFrameByteArray = BGR_TO_RGB(MatToByteArray(FishEyeToPanoramic(ResizeImage(frame), point)));
                    finalFrameByteArray = BGR_TO_RGB(MatToByteArray(ResizeImage(frame)));
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //finalFrameByteArray = BGR_TO_RGB(MatToByteArray(ResizeImage(frame)));
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
