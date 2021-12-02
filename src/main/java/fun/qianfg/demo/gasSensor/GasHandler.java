package fun.qianfg.demo.gasSensor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class GasHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        System.out.println("接收到消息：" + ByteBufUtil.hexDump(msg));
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

//                String[] gasDevices = new String[]{"121", "122", "123", "124", "xh2001"};
                String[] gasDevices = new String[]{"xh2001"};// 探头子系统索引
                for (String index : gasDevices) {
                    DecimalFormat df = new DecimalFormat("0.00");
                    String value = df.format(Math.random() * 20 + 55);   // 探测值范围
//                    String value = "0";   // 探测值范围
                    String cmd = "{\"RPTS\":[{\"devid\":\"aa1\",\"name\":\"" + index + "\",\"value\":\"" + value + "\",\"unit\":\"%LEL\",\"state\":\"1\",\"L\":\"20.0\",\"H\":\"50.0\"}]}";
                    char[] chars = cmd.toCharArray();
                    ByteBuf buf = Unpooled.buffer();
                    for (int param : chars) {
                        buf.writeByte(param);
                    }
//                System.out.println("发送的数据：" + ByteBufUtil.hexDump(buf));
                    System.out.println(cmd);
                    ctx.writeAndFlush(buf);
                }

            }
        }, 0, 10, TimeUnit.SECONDS);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
