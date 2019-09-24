package sample;

class CarState {
    public static final int MAX_SPEED = 100;
    public static final float MAX_STEERING_ANGLE = 45;
    private int speed;
    private int steerAngle;
    private int gear;
    private int brake;
    private boolean isHardBrake;
    private boolean isSteeringLeft, isSteeringRight;

    CarState(){
        this.speed = 0;
        this.steerAngle = 0;
        this.gear = 1;
        brake = 0;
        isHardBrake = false;
        isSteeringLeft = false;
        isSteeringRight = false;
    }

    public void setSteeringLeft(boolean steeringLeft) {
        isSteeringLeft = steeringLeft;
    }

    public void setSteeringRight(boolean steeringRight) {
        isSteeringRight = steeringRight;
    }

    public int getSpeed() {
        return speed;
    }

    public int getGear() {
        return gear;
    }

    public int getSteerAngle() {
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

    public void setSteerAngle(int steerAngle) {
        this.steerAngle = steerAngle;
    }

    public void setBrake(int brake) {
        this.brake = brake;
    }

    public void setHardBrake(boolean hardBrake) {
        isHardBrake = hardBrake;
    }

    public boolean isSteeringLeft() {
        return isSteeringLeft;
    }
    public boolean isSteeringRight() {
        return isSteeringRight;
    }
}