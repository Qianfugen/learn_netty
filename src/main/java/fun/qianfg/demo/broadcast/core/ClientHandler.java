package fun.qianfg.demo.broadcast.core;

import fun.qianfg.demo.broadcast.Response.DeviceInformation;
import fun.qianfg.demo.broadcast.Response.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

/**
 * 用于回复处理
 */
@SuppressWarnings("rawtypes")
public class ClientHandler extends SimpleChannelInboundHandler<ResponseMessage> {
    private static final Integer PDA_DEVICE_TYPE = 0x0F;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ResponseMessage responseMessage) {
        if (responseMessage.getMessage() instanceof DeviceInformation) {
            DeviceInformation response = (DeviceInformation) responseMessage.getMessage();

            if (Objects.equals(response.getType(), PDA_DEVICE_TYPE)) {
                ResponseFuture<ResponseMessage> future = RequestHolder.udpFuture;
                if (future != null) {
                    future.getPromise().setSuccess(responseMessage);
                }
            }
        }
    }
}
