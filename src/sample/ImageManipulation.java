package sample;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageManipulation {

    public static Mat FishEyeToPanoramic(Mat frame, int x, int y){
        double width = frame.rows();
        double height = frame.cols();
        double hh = height / 2;
        double hw = width / 2;

        double zoom = 1.3;
        double strength = 4.5;
        double r1 = Math.pow((height * height + width * width), 1 / 2) / strength;
        Mat panoramicFrame = new Mat((int)width,(int)height,CvType.CV_8UC3);
        double theta;
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                double newx = i - hw + x;
                double newy = j - hh + y;
                double distance = Math.pow((newx * newx + newy * newy), 1 / 2);
                double r = distance / r1;
                if (r == 0) {
                    theta = 1;
                }
                else {
                    theta = Math.atan(r) / r;
                }
                int sourcex = (int)Math.round(hw + theta * newx * zoom);
                int sourcey = (int)Math.round(hh + theta * newy * zoom);
                panoramicFrame.put(i,j, frame.get(sourcex, sourcey));
            }
        }
        return panoramicFrame;
    }

    public static Mat resizeImage(Mat frame, int width, int height){
        Mat resizedImage = new Mat(width, height, CvType.CV_8UC3);
        Size sz = new Size(Server.IMG_WIDTH, Server.IMG_HEIGHT);
        Imgproc.resize( frame, resizedImage, sz,Imgproc.INTER_AREA);
        return resizedImage;
    }

    public static byte[] MatToByteArray(Mat frame){
        byte[] buff = new byte[(int) (frame.total() * frame.channels())];
        frame.get(0, 0, buff);
        return buff;
    }
}
