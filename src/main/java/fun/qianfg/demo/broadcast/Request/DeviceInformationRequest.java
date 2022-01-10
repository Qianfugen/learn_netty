package fun.qianfg.demo.broadcast.Request;

public class DeviceInformationRequest extends RequestMessage {
    public DeviceInformationRequest() {
        msgID = 0xF007;
        encryptType = 0;
        isSubpackage = false;
    }

    @Override
    public void encode() {

    }
}
