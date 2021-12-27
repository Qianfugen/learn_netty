package fun.qianfg.chapter10.encoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Function: MessageToMessageDecoder, 两个消息格式之间进行转换
 *
 * @author qianfg
 * @date 2021/12/27 23:07
 * @Email: 287541326@qq.com
 */
public class IntegerToStringDecoder extends MessageToMessageDecoder<Integer> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Integer in, List<Object> out) {
        System.out.println("数字" + in + "转成字符串");
        out.add(String.valueOf(in));
    }
}
