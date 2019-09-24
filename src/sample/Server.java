package sample;

import org.opencv.core.Mat;
import org.opencv.video.Video;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import static sample.ImageManipulation.*;

public class Server {
    private ServerSocket serverSocket;
    private static int PORT = 8888;
//    private String IP = "192.168.1.22";
//    private String IP = "192.168.43.22";
    private String IP = "172.20.10.5";
    private String IP_VR;
    private String IP_CAR;
    private byte[] receivedData = null;
    public static int IMG_SIZE;
    public static int IMG_WIDTH = 100;
    public static int IMG_HEIGHT = 100;
    public static String CAR = "Car";
    public static Map<String, Socket> socketList;
    public static boolean isStreamingStarted = false;
    public static boolean isVrConnected = false;
    int count = 0;
    public static boolean isCarConnected = false;


    public Server() throws Exception {
        socketList = new HashMap<>();
        receivedData = new byte[5];
        serverSocket = new ServerSocket(PORT, 100, InetAddress.getByName(IP));
        System.out.println("Server Started");
        VideoStreaming.StartStreaming();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Socket clientSocket;
                    try {
                        clientSocket = serverSocket.accept();
                        handleClientRequest(clientSocket);
//                     wdwa

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }


    private void handleClientRequest(Socket socket) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int size = socket.getInputStream().read(receivedData);
                    if (size > 0) {
                        if(receivedData[0] == 'R'){
                            IP_VR = socket.getInetAddress().toString().substring(1);
                            Point resolution = decodeByteArray(receivedData);
                            IMG_WIDTH = (int) resolution.x;
                            IMG_HEIGHT = (int) resolution.y;
                            IMG_SIZE = IMG_WIDTH * IMG_HEIGHT * 3;
                            System.out.println(IMG_WIDTH + "    " + IMG_HEIGHT);
                            Thread.sleep(1000);
                            sendFrame(socket);
                            isVrConnected = true;
                        }
                        else if(receivedData[0] == 'P'){
                            IP_VR = socket.getInetAddress().toString().substring(1);
                            VideoStreaming.point = decodeByteArray(receivedData);
                            sendFrame(socket);
                        }
                        else if(receivedData[0] == 'C'){
                            IP_CAR = socket.getInetAddress().toString().substring(1);
                            socketList.put(CAR, socket);
                            isCarConnected = true;
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        thread.start();

    }

    public void sendData(byte[] data, Socket clientSocket) throws IOException {
        if(clientSocket.getInetAddress().toString().substring(1).equals(IP_VR)){
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.write(data);
                dataOutputStream.flush();
                dataOutputStream.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(clientSocket.getInetAddress().toString().substring(1).equals(IP_CAR)) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.write(data);
                dataOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFrame(Socket socket) {
        try {
             byte[] frame = CopyArray(VideoStreaming.finalFrameByteArray);
             sendData(frame, socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] CopyArray(byte[] b1){
        byte[] result = new byte[b1.length];
        for(int i = 0; i < result.length; i++){
            result[i] = b1[i];
        }
        return result;
    }



    private Point decodeByteArray(byte[] data) {
        Point point = new Point();
        if(data[0] == 'R'){
            point.x = (byteArrayToInt(new byte[]{data[2], data[1]}));
            point.y = (byteArrayToInt(new byte[]{data[4], data[3]}));
            return point;
        }
        else if(data[0] == 'P'){

           if(data[1] == 'P' && data[3] == 'P'){
               point.x = (data[2] & 0xff);
               point.y = (data[4] & 0xff);
               return point;
           }
           else if(data[1] == 'P' && data[3] == 'N') {
               point.x = (data[2] & 0xff);
               point.y = (data[4] & 0xff) * -1;
               return point;
           }
           else if(data[1] == 'N' && data[3] == 'P'){
                point.x = (data[2] & 0xff) * -1;
                point.y = (data[4] & 0xff);
                return point;
            }
           else if(data[1] == 'N' && data[3] == 'N'){
                point.x = (data[2] & 0xff) * -1;
                point.y = (data[4] & 0xff) * -1;
                return point;
            }
        }

        return null;
    }


    public static int byteArrayToInt(byte[] b) {
        if (b.length == 4)
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
                    | (b[3] & 0xff);
        else if (b.length == 2)
            return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

        return 0;
    }


}
