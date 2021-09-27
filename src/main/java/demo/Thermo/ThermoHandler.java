package demo.Thermo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThermoHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private DefaultEventExecutorGroup group;

    private int[][] cmds;

    private CountDownLatch latch;

    private ChannelHandlerContext ctx;


    public ThermoHandler() {
        this.group = new DefaultEventExecutorGroup(1);
        this.cmds = new int[][]{
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x31, 0x33, 0x38, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x32, 0x33, 0x39, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x33, 0x33, 0x41, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x34, 0x33, 0x42, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x35, 0x33, 0x43, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x36, 0x33, 0x44, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x37, 0x33, 0x45, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x38, 0x33, 0x46, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x39, 0x34, 0x30, 0x03},
                {0x07, 0x30, 0x31, 0x52, 0x4D, 0x31, 0x30, 0x36, 0x38, 0x03}
        };
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        System.out.println("接收到消息：" + ByteBufUtil.hexDump(msg));
        // 消息接受完毕后释放同步锁
        latch.countDown();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
        ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sentMsg();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void sentMsg() {
        // 异步，防止现场阻塞
        group.submit(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                for (int[] cmd : cmds) {
                    // 设置同步锁
                    latch = new CountDownLatch(1);

                    ByteBuf buf = Unpooled.buffer();
                    for (int param : cmd) {
                        buf.writeByte(param);
                    }
                    System.out.println("发送的数据：" + ByteBufUtil.hexDump(buf));
                    ctx.writeAndFlush(buf);

                    // 开启等待,会等待服务器返回结果之后再执行下面的代码
                    latch.await();
                }
                long endTime = System.currentTimeMillis();
                System.out.println("共耗时 " + String.format("%.2f", (endTime - startTime) / 1000.0) + " s");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
