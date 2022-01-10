package fun.qianfg.demo.broadcast;

import fun.qianfg.demo.broadcast.Request.DeviceInformationRequest;
import fun.qianfg.demo.broadcast.Response.ResponseMessage;
import fun.qianfg.demo.broadcast.core.NettyUdpClient;
import fun.qianfg.demo.broadcast.core.ResponseFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Test {
    public static void main(String[] args) {
        NettyUdpClient nettyUdpClient = new NettyUdpClient(14325, 14325);
        ResponseFuture<ResponseMessage> responseFuture = nettyUdpClient.broadcast(new DeviceInformationRequest());
        try {
            ResponseMessage response = responseFuture.getPromise().get(5, TimeUnit.SECONDS);
            log.info("接收到消息：{}", response.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nettyUdpClient.destroy();
        }
    }
}
