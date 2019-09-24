package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
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
    private Server server = null;

    private VideoStreaming videoStreaming;
    byte receivedData[];
    private CarState currentCarState;

    @FXML
    private Pane pane;

    @FXML
    private Text serverStatusText, carStatusText, vrStatusText;

    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        currentCarState = new CarState();
        pane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()){
                    case A:
                        currentCarState.setSteeringLeft(false);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                    if(!currentCarState.isSteeringLeft()){
                                        steerBalance();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();

                    case D:
                        currentCarState.setSteeringRight(false);
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                    if(!currentCarState.isSteeringRight()){
                                        steerBalance();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();
                }
            }
        });
        pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case W:
                        if (currentCarState.getSpeed() < CarState.MAX_SPEED) {
                            try {
                                accelerate(currentCarState.getSpeed() + 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case S:
                        if (currentCarState.getSpeed() > 0) {
                            try {
                                deAccelerate(currentCarState.getSpeed() - 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case A:
                        if ((currentCarState.getSteerAngle() > -CarState.MAX_STEERING_ANGLE) && currentCarState.getSpeed() != 0) {
                            try {
                                steerLeft(currentCarState.getSteerAngle() - 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case D:
                        if ((currentCarState.getSteerAngle() < CarState.MAX_STEERING_ANGLE)  && currentCarState.getSpeed() != 0) {
                            try {
                                steerRight(currentCarState.getSteerAngle() + 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case F:
                        if (currentCarState.getGear() != 1 && currentCarState.getSpeed() == 0) {
                            try {
                                forwardGear();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case R:
                        if (currentCarState.getGear() != 0 && currentCarState.getSpeed() == 0) {
                            try {
                                reverseGear();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case E:
                        if(currentCarState.getSpeed() != 0) {
                            try {
                                hardBrake();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;


                }
            }
        });


        //Set Status Text
        setStatus();

    }

    private void setStatus(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isConnected = false;
                while (true){
                    //displayFrames(VideoStreaming.getFrame());
                    if(Server.isCarConnected && !isConnected){
                        carStatusText.setText("Connected");
                        carStatusText.setStyle("-fx-font-color: blue;");
                        isConnected = true;
                    }
                    if(!Server.isCarConnected && isConnected){
                        carStatusText.setText("Not Connected");
                        carStatusText.setStyle("-fx-font-color: red;");
                        isConnected = false;
                    }
                    if(Server.isVrConnected){
                        vrStatusText.setText("Connected");
                        vrStatusText.setStyle("-fx-font-color: blue;");
                    }
                }
            }
        });
        thread.start();
    }


    @FXML
    private void startServer() {
        try {
            if(server == null) {
                server = new Server();
                serverStatusText.setText("Connected");
                serverStatusText.setStyle("-fx-font-color: blue;");
            }
        }
        catch (Exception e){
            serverStatusText.setText("Could't Connect");
            e.printStackTrace();
        }
    }

    private void accelerate(int speed) throws IOException {
        String value = addSeparator("A+");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSpeed(speed);
        currentCarState.setSteerAngle(0);
    }

    private void deAccelerate(int speed) throws IOException {
        String value = addSeparator("A-");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSpeed(speed);
        currentCarState.setSteerAngle(0);
    }

    private void steerLeft(int steerAngle) throws IOException {
        currentCarState.setSteeringLeft(true);
        String value = addSeparator("S-");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSteerAngle(steerAngle);
    }

    private void steerRight(int steerAngle) throws IOException {
        currentCarState.setSteeringRight(true);
        String value = addSeparator("S+");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSteerAngle(steerAngle);
    }


    private void hardBrake() throws IOException {
        String value = addSeparator("HB");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSpeed(0);
        currentCarState.setSteerAngle(0);
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

    private void steerBalance() throws IOException {
        String value = addSeparator("SB");
        server.sendData(value.getBytes(), Server.socketList.get(Server.CAR));
        currentCarState.setSteerAngle(0);
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


