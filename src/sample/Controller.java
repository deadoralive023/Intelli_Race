package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private Server server;

    private VideoStreaming videoStreaming;
    byte receivedData[];
    private CarState currentCarState;

    @FXML
    private Pane pane;

    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        currentCarState = new CarState();
        pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case W:
                        if (currentCarState.getSpeed() < CarState.MAX_SPEED) {
                            try {
                                accelerate();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case S:
                        if (currentCarState.getSpeed() > 0) {
                            try {
                                deAccelerate();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case A:
                        if (currentCarState.getSteerAngle() > -CarState.MAX_STEERING_ANGLE) {
                            try {
                                steerLeft();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case D:
                        if (currentCarState.getSteerAngle() < CarState.MAX_STEERING_ANGLE) {
                            try {
                                steerRight();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case P:
                        if (currentCarState.getGear() != 1 && currentCarState.getSpeed() < 10) {
                            try {
                                forwardGear();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case L:
                        if (currentCarState.getGear() != 0 && currentCarState.getSpeed() < 10) {
                            try {
                                reverseGear();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case B:
                            try {
                               hardBrake();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        break;

                }
            }
        });

    }


    @FXML
    private void startServer() throws Exception {
        server = new Server();
    }

    private void accelerate() throws IOException {
        String value = addSeparator("A+");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSpeed(currentCarState.getSpeed() + 1);
    }

    private void deAccelerate() throws IOException {
        String value = addSeparator("A-");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSpeed(currentCarState.getSpeed() - 1);
    }

    private void steerLeft() throws IOException {
        String value = addSeparator("S-");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSteerAngle(currentCarState.getSteerAngle() - 1);
    }

    private void steerRight() throws IOException {
        String value = addSeparator("S+");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSteerAngle(currentCarState.getSteerAngle() + 1);
    }


    private void hardBrake() throws IOException {
        String value = addSeparator("HB");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSpeed(0);
    }

    private void forwardGear() throws IOException {
        String value = addSeparator("FG");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setGear(1);
    }

    private void reverseGear() throws IOException {
        String value = addSeparator("RG");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setGear(0);
    }

    private String addSeparator(String value) {
        value = value + "/";
        return value;
    }


    private void displayFrames(Mat frame){
        //Display Frame
        BufferedImage bufferedImage = VideoStreaming.Mat2BufferedImage(frame);
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        imageView.setImage(image);
    }

}

class CarState {
    public static final int MAX_SPEED = 100;
    public static final float MAX_STEERING_ANGLE = 45;
    private int speed;
    private float steerAngle;
    private int gear;
    private int brake;
    private boolean isHardBrake;

    CarState(){
        this.speed = 0;
        this.steerAngle = 0;
        this.gear = 1;
        brake = 0;
        isHardBrake = false;
    }

    CarState(int acceleration, int steerAngle, int gear, int brake, boolean isHardBrake){
        this.speed = acceleration;
        this.steerAngle = steerAngle;
        this.gear = gear;
        this.brake = brake;
        this.isHardBrake = isHardBrake;
    }

    public int getSpeed() {
        return speed;
    }

    public int getGear() {
        return gear;
    }

    public float getSteerAngle() {
        return steerAngle;
    }

    public int getBrake() {
        return brake;
    }

    public boolean isHardBrake() {
        return isHardBrake;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setGear(int gear) {
        this.gear = gear;
    }

    public void setSteerAngle(float steerAngle) {
        this.steerAngle = steerAngle;
    }

    public void setBrake(int brake) {
        this.brake = brake;
    }

    public void setHardBrake(boolean hardBrake) {
        isHardBrake = hardBrake;
    }
}


