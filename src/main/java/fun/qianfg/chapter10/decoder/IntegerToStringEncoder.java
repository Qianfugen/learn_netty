package fun.qianfg.chapter10.decoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Function:编码器，继承自MessageToMessageDecoder,将消息编码为另一种消息格式
 *
 * @author qianfg
 * @date 2021/12/28 20:43
 * @Email: 287541326@qq.com
 */
public class IntegerToStringEncoder extends MessageToMessageEncoder<Integer> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Integer msg, List<Object> out) {
        out.add(String.valueOf(msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
