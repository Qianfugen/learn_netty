package RFID.cmd;

import RFID.RFIDCmd;

/**
 * 心跳
 * 40 03 0a 01 b2
 */
public class HeartBeatCmd extends RFIDCmd {

    /**
     * 读写器地址,1～254，0和255为广播地址
     */
    private int address = 0x01;

    /**
     * 构造方法，初始化各参数值
     */
    public HeartBeatCmd() {
        this.setLength(0x03);
        this.setCmd(0x0a);
        this.setData(new int[]{address});
        this.setCheckSum(this.calculateCheckSum());
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }
}
