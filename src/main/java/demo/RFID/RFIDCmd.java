package demo.RFID;

/**
 * RFID通用命令
 * address不一定在length后面（writeTagCmd），所以把它放到data里面
 */
public class RFIDCmd {

    /**
     * 引导码，固定为40H
     */
    private int bootCode = 0x40;

    /**
     * 包有效长度，该长度为后4个部分的总字节数
     */
    private int length;

    /**
     * 命令码
     */
    private int cmd;

    /**
     * 命令参数，其长度随命令而变化
     */
    private int[] data;

    /**
     * 校验和，为从引导码(bootCode)开始到命令参数(data)全部字节总和、丢弃进位后的字节补码
     */
    private int checkSum;

    /**
     * 计算校验和的值
     *
     * @return
     */
    public int calculateCheckSum() {
        int sum = bootCode + length + cmd;
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                sum += data[i];
            }
        }
        return 256 - sum % 256;
    }

    public int getBootCode() {
        return bootCode;
    }

    public void setBootCode(int bootCode) {
        this.bootCode = bootCode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public int getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }
}
