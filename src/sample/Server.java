package sample;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

import static sample.ImageManipulation.MatToByteArray;

public class Server {
    private ServerSocket serverSocket;
    private BufferedReader br;
    private BufferedWriter bw;
    public static int PORT = 8888;
    public static final String IP = "192.168.1.22";
    public static final String IP_VR = "192.168.1.22";
    public static final String IP_CAR = "192.168.1.20";
    private Map<String, ClientSocket> clients;
    public static final String CAR = "Car";
    public static final String VR = "VR";
    private byte[] receivedData = null;
    private final int BUFF_SIZE = 60000;
    private static int IMG_SIZE;
    public static int IMG_WIDTH;
    public static int IMG_HEIGHT;


    public Server() throws Exception {
        clients = new HashMap<>();
        receivedData = new byte[5];
        serverSocket = new ServerSocket(PORT, 100, InetAddress.getByName(IP));
        System.out.println("Server Started");
        VideoStreaming.StartStreaming();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    final Socket activeSocket;
                    try {
                        activeSocket = serverSocket.accept();
                        Runnable runnable = () -> handleClientRequest(activeSocket);
                        new Thread(runnable).start(); // start a new thread
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void handleClientRequest(Socket socket) {
            try {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                int size = socket.getInputStream().read(receivedData);
                if (size > 0) {
                    switch (receivedData[0]) {
                        case 'R':
                            //only call once
                            //clients.put(VR, new ClientSocket(socket.getInetAddress().toString().substring(1), socket.getPort(), VR));
                            Point resolution = decodeByteArray(receivedData);
                            IMG_WIDTH = (int) resolution.x;
                            IMG_HEIGHT = (int) resolution.y;
                            IMG_SIZE = IMG_WIDTH * IMG_HEIGHT * 3;
                            startSendingVideoStreaming(socket);
                            break;
                        case 'P':
                            Point point = decodeByteArray(receivedData);
                            break;
                        case 'C':
                            //Car
                            //clients.put(CAR, new ClientSocket(receivePacket.getAddress().toString().substring(1), receivePacket.getPort(), CAR));
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
    }

    public void sendData(byte[] data, Socket clientSocket) throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                switch (clientSocket.getInetAddress().toString().substring(1)) {
                    case IP_VR:
                        //send frames in multiple packets
                        byte[] packet;
                        int index = 0;
                        while (index < IMG_SIZE) {
                            packet = Arrays.copyOfRange(data, index, (index = (index + BUFF_SIZE <= IMG_SIZE) ? index + BUFF_SIZE :  data.length));
                            try {
                                clientSocket.getOutputStream().write(packet);
                                clientSocket.getOutputStream().flush();
                                Thread.sleep(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case IP_CAR:
                        try {
//                            sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(clientSocket.getIp()), clientSocket.getPort());
//                            server.send(sendPacket);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        thread.start();
    }

    private void  startSendingVideoStreaming(Socket socket){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Mat frame = null;
                while (VideoStreaming.getCapture().isOpened()) {
                    frame = VideoStreaming.getFrame();
                    if(frame != null){
                        try {
                            sendData(MatToByteArray(ImageManipulation.resizeImage(frame, Server.IMG_WIDTH, Server.IMG_HEIGHT)), socket);
                            //sendData(MatToByteArray(ImageManipulation.FishEyeToPanoramic(ImageManipulation.resizeImage(frame, Server.IMG_WIDTH, Server.IMG_HEIGHT),0,0)), socket);
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
    }



    private Point decodeByteArray(byte[] data) {
        Point point = new Point();
        point.x = (byteArrayToInt(new byte[]{data[2], data[1]}));
        point.y = (byteArrayToInt(new byte[]{data[4], data[3]}));
        return point;
    }


    public Map<String, ClientSocket> getClients() {
        return clients;
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
