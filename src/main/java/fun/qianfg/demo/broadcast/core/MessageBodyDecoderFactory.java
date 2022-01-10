package fun.qianfg.demo.broadcast.core;


import fun.qianfg.demo.broadcast.bodydecoder.BaseMessageBodyDecoder;
import fun.qianfg.demo.broadcast.bodydecoder.DeviceInformationMessageBodyDecoder;

/**
 * decoder工厂类，实现消息体编译器的生成
 */
public enum MessageBodyDecoderFactory {
    Instance;

    public BaseMessageBodyDecoder getMessage(int messageId) {
        switch (messageId) {
            case 7:
                return new DeviceInformationMessageBodyDecoder();
            default:
                throw new RuntimeException("暂不支持的类型");
        }
    }
}
