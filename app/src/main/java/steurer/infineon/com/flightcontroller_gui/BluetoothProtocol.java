package steurer.infineon.com.flightcontroller_gui;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SteurerE on 03.02.2015.
 * <p/>
 * Class for implementing a Communication Protocol
 * based on byte-streams.
 * <p/>
 * It's for Communication with a XMC4500 Microcontroller
 * over a Bluetooth Serial Connection
 */

public class BluetoothProtocol {

    public static boolean USE_MSP_PROTOCOL = false;

    public static final int PACKET_SIZE = 16; //19 bytes for the Protocol
    public static final int DATA_SIZE = 14; //14 bytes data for data-package
    public static final int HEADER_SIZE = 1; //1 byte header
    public static final int CHECKSUM_SIZE = 4; //4 bytes checksum
    public static final byte CONTROL_HEADER = 0x00;

    public static byte[] prepareControlPackage(ControlPacket packet) {
        byte[] b = new byte[PACKET_SIZE];

        if (USE_MSP_PROTOCOL) {
            int speed = 10*packet.getSpeed()+1000;
            byte arm = packet.getArm();
            int azimuth = (int)(-1000.0/360.0*packet.getAzimuth()+1500);
            int pitch = (int)(-1000.0/90.0*packet.getPitch()+1500);
            int roll = (int)(-1000.0/90.0*packet.getRoll()+1500);

            b[0] = (byte) 0x24;
            b[1] = (byte) 0x4D;
            b[2] = (byte) 0x3C;
            b[3] = (byte) 0x0A;
            b[4] = (byte) 0xC8;

            b[5] = (byte) pitch;
            b[6] = (byte) (pitch >> 8);
            b[7] = (byte) roll;
            b[8] = (byte) (roll >> 8);
            b[9] = (byte) speed;
            b[10] = (byte) (speed >> 8);
            b[11] = (byte) azimuth;
            b[12] = (byte) (azimuth >> 8);

            if (arm == 0x01) {
                b[13] = (byte) 0xd0;
                b[14] = (byte) 0x07;
            } else {
                b[13] = (byte) 0xe8;
                b[14] = (byte) 0x03;
            }

            byte checksum = 0;

            for (int i = 3; i < PACKET_SIZE - 1; i++)
                checksum ^= b[i];

            b[15] = checksum;
        }
        else
        {
            int speed = (int)((1738.0-306.0)/100.0*packet.getSpeed()+306.0);
            byte arm = packet.getArm();
            int roll = (int)(-(3786.0-2352.0)/90.0*packet.getPitch()+3070);
            int pitch = (int)(-(5836.0-4404.0)/90.0*packet.getRoll()+5118);
            int azimuth = (int)(-(7878.0-6446.0)/180.0*packet.getAzimuth()+7162);

            if (roll < 2352)
                roll = 2352;
            if (roll > 3786)
                roll = 3786;
            if (pitch < 4404)
                pitch = 4404;
            if (pitch > 5836)
                pitch = 5836;

            b[0] = (byte) 0x00;
            b[1] = (byte) 0xa2;
            b[2] = (byte) (roll >> 8);
            b[3] = (byte) roll;
            b[4] = (byte) 0x30;
            b[5] = (byte) 0x00;
            b[6] = (byte) (pitch >> 8);
            b[7] = (byte) pitch;
            if (arm == 0x01) {
                b[8] = (byte) 0x26;
                b[9] = (byte) 0xCA;
            }
            else {
                b[8] = (byte) 0x21;
                b[9] = (byte) 0x32;
            }
            b[10] = (byte) (azimuth >> 8);;
            b[11] = (byte) azimuth;
            b[12] = (byte) 0x2b;
            b[13] = (byte) 0xfe;
            b[14] = (byte) (speed >> 8);
            b[15] = (byte) speed;
        }

        return b;
    }

    public static List<byte[]> prepareDataPackages(DataPacket packet) {
        String all_data = packet.getData();
        List<String> packetlist = new ArrayList<String>();
        List<byte[]> bytelist = new ArrayList<byte[]>();
        int mod = 0;
        if (all_data.length() > DATA_SIZE) {
            int length = all_data.length() / DATA_SIZE;
            mod = all_data.length() % DATA_SIZE;
            if (mod == 0) {
                for (int i = 0; i < length; i++) {
                    packetlist.add(all_data.substring(i * DATA_SIZE, (i + 1) * DATA_SIZE));
                }
            } else {
                for (int i = 0; i < (length + 1); i++) {
                    if (i == length) {
                        String s = all_data.substring(i * DATA_SIZE, i * DATA_SIZE + mod);
                        char[] stringbuffer = s.toCharArray();
                        char[] last_packet = new char[DATA_SIZE];
                        System.arraycopy(stringbuffer, 0, last_packet, 0, mod);
                        String last_packet_s = new String(last_packet);
                        packetlist.add(last_packet_s);
                    } else {
                        packetlist.add(all_data.substring(i * DATA_SIZE, (i + 1) * DATA_SIZE));
                    }
                }
            }
        } else {
            mod = all_data.length();
            char[] stringbuffer = all_data.toCharArray();
            char[] last_packet = new char[DATA_SIZE];
            System.arraycopy(stringbuffer, 0, last_packet, 0, stringbuffer.length);
            String last_packet_s = new String(last_packet);
            packetlist.add(last_packet_s);
        }
        int start_byte_count = 0;
        if (packetlist.size() == 1) {
            start_byte_count = mod;
        } else if (mod == 0) {
            start_byte_count = DATA_SIZE * packetlist.size();
        } else if (mod != 0) {
            start_byte_count = DATA_SIZE * (packetlist.size() - 1) + mod;
        }
        for (int i = 0; i < packetlist.size(); i++) {
            byte[] dest = new byte[PACKET_SIZE];
            byte[] src = packetlist.get(i).getBytes();
            byte[] header = new byte[1];
            header[0] = (byte) start_byte_count;
            System.arraycopy(header, 0, dest, 0, 1);
            System.arraycopy(src, 0, dest, 1, src.length);
            bytelist.add(dest);
            start_byte_count = start_byte_count - DATA_SIZE;
        }
        for (int i = 0; i < bytelist.size(); i++) {
            byte[] checksum_byte = new byte[CHECKSUM_SIZE];
            byte[] checksum_helper = bytelist.get(i);
            int checksum = checksum_helper[0];
            for (int j = HEADER_SIZE; j < PACKET_SIZE - CHECKSUM_SIZE; j += 4) {
                checksum ^= (checksum_helper[j] << 24) | (checksum_helper[j + 1] << 16) | (checksum_helper[j + 2] << 8) | (checksum_helper[j + 3]);
            }
            checksum_byte[0] = (byte) ((checksum >> 24) & 0xFF);
            checksum_byte[1] = (byte) ((checksum >> 16) & 0xFF);
            checksum_byte[2] = (byte) ((checksum >> 8) & 0xFF);
            checksum_byte[3] = (byte) (checksum & 0xFF);
            System.arraycopy(checksum_byte, 0, checksum_helper, PACKET_SIZE - CHECKSUM_SIZE, checksum_byte.length);
        }
        return bytelist;
    }
}
