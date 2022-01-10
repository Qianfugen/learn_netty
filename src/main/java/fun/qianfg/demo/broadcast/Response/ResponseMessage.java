package fun.qianfg.demo.broadcast.Response;

import lombok.Data;

@Data
public class ResponseMessage<T> {
    public ResponseMessage() {
        this.versionNO = 1;
    }

    public ResponseMessage(T message) {
        this();
        this.message = message;
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
    private T message;

}
