package sample;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class ImageManipulation {

    private static Mat resizedImage = new Mat(Server.IMG_WIDTH, Server.IMG_HEIGHT, CvType.CV_8UC3);
    public static Mat FishEyeToPanoramic(Mat frame, Point point){
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
                double newX = i - hw + point.x;
                double newY = j - hh + point.y;
                double distance = Math.pow((newX * newX + newY * newY), 1 / 2);
                double r = distance / r1;
                if (r == 0) {
                    theta = 1;
                }
                else {
                    theta = Math.atan(r) / r;
                }
                int sourceX = (int)Math.round(hw + theta * newX * zoom);
                int sourceY = (int)Math.round(hh + theta * newY * zoom);
                panoramicFrame.put(i,j, frame.get(sourceX, sourceY));
            }
        }
        return panoramicFrame;
    }

    public static Mat ResizeImage(Mat frame){
        while (frame.rows() == 0 || frame.cols() == 0){
            frame = VideoStreaming.getFrame();
        }
        Imgproc.resize(frame, resizedImage, new Size(Server.IMG_WIDTH,Server.IMG_HEIGHT),Imgproc.INTER_AREA);
        return resizedImage;
    }

    public static byte[] MatToByteArray(Mat frame){
        byte[] buff = new byte[(int) (frame.total() * frame.channels())];
        frame.get(0, 0, buff);
        return buff;
    }



    public static byte[] BGR_TO_RGB(byte[] BGR){
        byte[] RGB = new byte[BGR.length];
        for(int i = 0; i < BGR.length; i = i + 3){
            RGB[i] = BGR[i + 2];
            RGB[i + 1] = BGR[i + 1];
            RGB[i + 2] = BGR[i];
        }
        return RGB;
    }
}
