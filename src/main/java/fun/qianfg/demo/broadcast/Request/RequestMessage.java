package fun.qianfg.demo.broadcast.Request;

public class RequestMessage {

    public RequestMessage() {
        this.versionNO = 1;
        deviceAddress = new int[]{0, 0, 0, 0,0,0};
    }

    /**
     * 协议版本号。当前版本为v1.0
     */
    protected int versionNO;

    /**
     * 消息ID，代表消息类型
     */
    protected int msgID;

    /**
     * 消息体长度
     */
    protected int bodyLength;

    /**
     * 数据加密方式，0代表消息体不加密，1代表消息体经过RSA算法加密
     */
    protected int encryptType;

    /**
     * 是否分包
     */
    protected boolean isSubpackage;

    /**
     * 设备类型
     */
    protected int deviceType;

    /**
     * MAC地址
     */
    protected int[] deviceAddress;

    /**
     * 消息流水号
     */
    protected int serialNO;

    /**
     * 消息分包后的总包数
     */
    protected int subPackageAmount;

    /**
     * 分包序号
     */
    protected int subPackageNumber;

    /**
     * 消息体
     */
    protected int[] msgBody;

    public  void encode() {

    }

    public int getVersionNO() {
        return versionNO;
    }

    public void setVersionNO(int versionNO) {
        this.versionNO = versionNO;
    }

    public boolean getIsSubpackage() {
        return isSubpackage;
    }

    public void setIsSubPackage(boolean isSubpackage) {
        this.isSubpackage = isSubpackage;
    }

    public int getMsgID() {
        return msgID;
    }

    public void setMsgID(int msgID) {
        this.msgID = msgID;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public int getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(int encryptType) {
        this.encryptType = encryptType;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int[] getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(int[] deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getSerialNO() {
        return serialNO;
    }

    public void setSerialNO(int serialNO) {
        this.serialNO = serialNO;
    }

    public int getSubPackageAmount() {
        return subPackageAmount;
    }

    public void setSubPackageAmount(int subPackageAmount) {
        this.subPackageAmount = subPackageAmount;
    }

    public int getSubPackageNumber() {
        return subPackageNumber;
    }

    public void setSubPackageNumber(int subPackageNumber) {
        this.subPackageNumber = subPackageNumber;
    }

    public int[] getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(int[] msgBody) {
        this.msgBody = msgBody;
    }

    public void setSubpackage(boolean isSubpackage) {
        this.isSubpackage = isSubpackage;
    }

}
