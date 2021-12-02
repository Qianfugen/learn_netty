package fun.qianfg.demo.usb;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class RxtxClient {

    private RxtxChannel channel;

    public static void main(String[] args) throws Exception {
        RxtxClient client = new RxtxClient();
        client.start();
    }

    public void start() throws Exception {
        //这里EventLoopGroup只能用阻塞式的，串口不支持非阻塞的EventLoopGroup
        EventLoopGroup group = new OioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    //需要通过Channel工厂来指定返回的channel
                    .channelFactory(new ChannelFactory<RxtxChannel>() {
                        public RxtxChannel newChannel() {
                            return channel;
                        }
                    })
                    .handler(new ChannelInitializer<RxtxChannel>() {
                        @Override
                        public void initChannel(RxtxChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    // 以换行符作为切分符
                                    new LineBasedFrameDecoder(32768),
                                    new StringEncoder(),    //串口发送String
                                    new StringDecoder(),
                                    new RxtxClientHandler()
                            );
                        }
                    });

            channel = new RxtxChannel();
            // 设置channel的基本属性，波特率，数据位，停止位这些
            channel.config()
                    .setBaudrate(9600) //波特率
                    .setDatabits(RxtxChannelConfig.Databits.DATABITS_8) //数据位
                    .setParitybit(RxtxChannelConfig.Paritybit.NONE) //校验位
                    .setStopbits(RxtxChannelConfig.Stopbits.STOPBITS_1);    //停止位

            //这里连接串口的名字，一般是“COM1” “COM2”这些
            ChannelFuture future = b.connect(new RxtxDeviceAddress("COM3")).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
