package fun.qianfg.chapter10.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * Function:
 *
 * @author qianfg
 * @date 2021/12/28 23:40
 * @Email: 287541326@qq.com
 */
public class CombineByteCharCodec extends CombinedChannelDuplexHandler<CombineByteCharCodec.ByteToCharDecoder, CombineByteCharCodec.CharToByteEncoder> {

    public CombineByteCharCodec() {
        super(new ByteToCharDecoder(), new CharToByteEncoder());
    }

    /**
     * 解码器
     */
    static class ByteToCharDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            while (in.readableBytes() >= 2) {
                out.add(in.readChar());
            }
        }
    }

    /**
     * 编码器
     */
    static class CharToByteEncoder extends MessageToByteEncoder<Character> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Character msg, ByteBuf out) {
            out.writeChar(msg);
        }
    }

}
