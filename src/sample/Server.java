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
    public static int PORT = 8888;
    public static final String IP = "192.168.1.22";
    public static final String IP_VR = "192.168.1.22";
    public static final String IP_CAR = "192.168.1.20";
    public static int BUFF_SIZE = 1000;
    private byte[] receivedData = null;
    public static int IMG_SIZE;
    public static int IMG_WIDTH = 100;
    public static int IMG_HEIGHT = 100;
    public static String CAR = "Car";
    public static Map<String, Socket> socketList;
    public static boolean isStreamingStarted = false;


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
                        switch (receivedData[0]) {
                            case 'R':
                                Point resolution = decodeByteArray(receivedData);
                                IMG_WIDTH = (int) resolution.x;
                                IMG_HEIGHT = (int) resolution.y;
                                IMG_SIZE = IMG_WIDTH * IMG_HEIGHT * 3;
                                System.out.println(IMG_WIDTH + "    " + IMG_HEIGHT);
                                Thread.sleep(1000);
                                sendFrame(socket, new java.awt.Point(0,0));
                                break;
                            case 'P':
                                VideoStreaming.point = decodeByteArray(receivedData);
                                sendFrame(socket, new Point(receivedData[2], receivedData[3]));
                                break;
                            case 'C':
                                //Car
                                socketList.put(CAR, socket);
                                break;
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
        switch (clientSocket.getInetAddress().toString().substring(1)) {
            case IP_VR:
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    dataOutputStream.write(data);
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    clientSocket.close();
                    //System.out.println("S");
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                        byte[] packet;
//                        int index = 0;
//                        DataOutputStream dataOutputStream = null;
//                        try {
//                            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        while (index < IMG_SIZE) {
//                            packet = Arrays.copyOfRange(data, index, (index = (index + BUFF_SIZE <= IMG_SIZE) ? index + BUFF_SIZE :  data.length));
//                            try {
//                                dataOutputStream.write(packet);
//                                dataOutputStream.flush();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
                break;
            case IP_CAR:
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    dataOutputStream.write(data);
                    dataOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;

        }

    }

    private void sendFrame(Socket socket, Point point) {
        try {
            sendData(VideoStreaming.getFinalFrame(), socket);
            //sendData(BGR_TO_RGB(MatToByteArray(ResizeImage(FishEyeToPanoramic(frame,new Point(1,1))))), socket);
//                    sendData(BGR_TO_RGB(MatToByteArray(ResizeImage(frame))), socket);
//                    sendData(BGR_TO_RGB(MatToByteArray(FishEyeToPanoramic(ResizeImage(frame), point)), socket));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

class ClientSocket{
    private String ip;
    private int port;
    private String clientName;

    public ClientSocket(String ip, int port, String clientName){
        this.ip = ip;
        this.port = port;
        this.clientName = clientName;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public String getClientName() {
        return clientName;
    }
}
