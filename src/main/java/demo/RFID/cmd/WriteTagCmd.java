package demo.RFID.cmd;

import demo.RFID.RFIDCmd;

import java.util.ArrayList;
import java.util.List;

/**
 * 写标签命令
 * 只需要填写oldTag和newTag即可
 */
public class WriteTagCmd extends RFIDCmd {

    /**
     * 原标签长度
     */
    private int oldLen = 0x06;

    /**
     * 原标签
     */
    private String oldTag;

    /**
     * 待写入的新标签长度
     */
    private int newLen = 0x06;

    /**
     * 待写入的新标签
     */
    private String newTag;

    /**
     * 选择数据区
     * 0 密码器
     * 1 EPC号
     * 2 TID标签ID号
     * 3 用户区User
     */
    private int memBank = 0x01;

    /**
     * 读写器地址,1～254，0和255为广播地址
     */
    private int address = 0x00;

    /**
     * 访问密码
     */
    private String accessPassword = "00000000";

    public WriteTagCmd(String oldTag, String newTag) {
        this.setOldTag(oldTag);
        this.setNewTag(newTag);
        this.setLength(0x22);
        this.setCmd(0xeb);
        this.setData(encode());
        this.setCheckSum(calculateCheckSum());
    }

    public WriteTagCmd() {

    }

    private int[] encode() {
        int oldLen = this.oldLen;
        int newLen = this.newLen;
        String oldTag = this.oldTag;
        String newTag = this.newTag;
        String accessPassword = this.accessPassword;
        int address = this.address;
        int memBank = this.memBank;

        List<Integer> params = new ArrayList<Integer>();
        params.add(oldLen);
        params.addAll(parseHexStringToInteger(oldTag));
        params.add(memBank);
        params.add(address);
        params.add(newLen);
        params.addAll(parseHexStringToInteger(newTag));
        params.addAll(parseHexStringToInteger(accessPassword));

        int[] rlt = new int[params.size()];
        for (int i = 0; i < params.size(); i++) {
            rlt[i] = params.get(i);
        }

        return rlt;
    }

    private List<Integer> parseHexStringToInteger(String hexString) {
        List<Integer> rlt = new ArrayList<Integer>();
        for (int i = 0; i < hexString.length(); i = i + 2) {
            String s = hexString.substring(i, i + 2);
            rlt.add(Integer.parseInt(s, 16));
        }
        return rlt;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getOldLen() {
        return oldLen;
    }

    public void setOldLen(int oldLen) {
        this.oldLen = oldLen;
    }

    public String getOldTag() {
        return oldTag;
    }

    public void setOldTag(String oldTag) {
        this.oldTag = oldTag;
    }

    public String getNewTag() {
        return newTag;
    }

    public void setNewTag(String newTag) {
        this.newTag = newTag;
    }

    public int getNewLen() {
        return newLen;
    }

    public void setNewLen(int newLen) {
        this.newLen = newLen;
    }

    public String getAccessPassword() {
        return accessPassword;
    }

    public void setAccessPassword(String accessPassword) {
        this.accessPassword = accessPassword;
    }

    public int getMemBank() {
        return memBank;
    }

    public void setMemBank(int memBank) {
        this.memBank = memBank;
    }
}
