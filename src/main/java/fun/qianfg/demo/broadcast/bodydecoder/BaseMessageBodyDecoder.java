package fun.qianfg.demo.broadcast.bodydecoder;

/**
 * 消息体解码器，用于多态实现
 *
 * @param <T>
 */
public interface BaseMessageBodyDecoder<T> {
    T decode(int[] byteBuf);
}
