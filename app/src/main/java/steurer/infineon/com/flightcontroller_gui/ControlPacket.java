package steurer.infineon.com.flightcontroller_gui;

/**
 * Created by SteurerE on 04.02.2015.
 */
public class ControlPacket
{


    private byte speed;
    private byte arm;
    private float azimuth;
    private float pitch;
    private float roll;

    public byte getArm() {
        return arm;
    }

    public void setArm(byte arm) {
        this.arm = arm;
    }

    public ControlPacket(byte speed,byte arm, float azimuth, float pitch, float roll)
    {
        this.speed = speed;
        this.arm = arm;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
    }


    public ControlPacket(){}

    public byte getSpeed() {
        return speed;
    }

    public void setSpeed(byte speed) {
        this.speed = speed;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }
}
