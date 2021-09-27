package demo.RFID.cmd;

import demo.RFID.RFIDCmd;

/**
 * 读标签命令
 * 40 06 ee 01 00 00 00 cb
 */
public class ReadTagCmd extends RFIDCmd {

    /**
     * 读写器地址,1～254，0和255为广播地址
     */
    private int address = 0x01;

    public ReadTagCmd() {
        this.setLength(0x06);
        this.setCmd(0xee);
        this.setData(new int[]{address, 0x00, 0x00, 0x00});
        this.setCheckSum(this.calculateCheckSum());
    }
}
