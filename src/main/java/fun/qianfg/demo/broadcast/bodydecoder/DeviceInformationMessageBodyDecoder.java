package fun.qianfg.demo.broadcast.bodydecoder;

import fun.qianfg.demo.broadcast.Response.DeviceInformation;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

@Slf4j
public class DeviceInformationMessageBodyDecoder implements BaseMessageBodyDecoder<DeviceInformation> {

    @Override
    public DeviceInformation decode(int[] body) {
        DeviceInformation deviceInformation = new DeviceInformation();
        int offset = 0;
        deviceInformation.setType(body[offset++]);
        int serialLength = body[offset++];
        if (serialLength != 0) {
            byte[] serialBytes = new byte[serialLength];
            for (; offset < serialLength + 2; offset++) {
                serialBytes[offset - 2] = (byte) body[offset];
            }
            try {
                deviceInformation.setDeviceSerialNumber(new String(serialBytes, "GBK"));
            } catch (UnsupportedEncodingException e) {
                log.error("DeviceInformationMessageBodyDecoder发送异常：{}", e.getMessage());
            }
        }
        int numberLength = body[offset++];

        if (numberLength != 0) {
            byte[] numberBytes = new byte[numberLength];
            for (; offset < serialLength + 2; offset++) {
                numberBytes[offset - 2] = (byte) body[offset];
            }

            try {
                deviceInformation.setNumber(new String(numberBytes, "GBK"));
            } catch (UnsupportedEncodingException e) {
                log.error("DeviceInformationMessageBodyDecoder发送异常：{}", e.getMessage());
            }
        }

        return deviceInformation;
    }
}
