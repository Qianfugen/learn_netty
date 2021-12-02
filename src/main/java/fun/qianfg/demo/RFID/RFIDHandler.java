package fun.qianfg.demo.RFID;

import fun.qianfg.demo.RFID.cmd.HeartBeatCmd;
import fun.qianfg.demo.RFID.cmd.ReadTagCmd;
import fun.qianfg.demo.RFID.cmd.WriteTagCmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.util.concurrent.TimeUnit;

public class RFIDHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    DefaultEventExecutorGroup eventExecutors = new DefaultEventExecutorGroup(4);

    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        final HeartBeatCmd heartBeatCmd = new HeartBeatCmd();
        // 每个20s发送一次心跳
        ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                ctx.write(heartBeatCmd);
            }
        }, 0, 20, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        // 接受client的消息
        ByteBuf buf = msg.content();
        String res = ByteBufUtil.hexDump(buf);
        System.out.println("接收来自RFID读写器的数据:" + res);

        if (res.equals("f0020a04")) {
            System.out.println("心跳成功");
//            readTag(0L);
        }

        if (res.startsWith("f010ee0106")) {
            System.out.println("读取成功");
            String label = res.substring(10, res.length() - 2);
            System.out.println("读取的标签号：" + label);
//            writeTag(label, "d12340000000000066666666", 1000L);
        }

//        if (res.equals("f002eb23")) {
//            System.out.println("写入成功");
//        }
//
//        if (res.equals("f403eb021c")) {
//            System.out.println("写入失败，重新写入");
//            writeTag(label, "d12340000000000066666666", 1000L);
//        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
    }

    public void readTag(Long sleepTime) throws InterruptedException {
        final ReadTagCmd readTagCmd = new ReadTagCmd();
        eventExecutors.submit(new Runnable() {
            @Override
            public void run() {
                ctx.writeAndFlush(readTagCmd);
            }
        });
        Thread.sleep(sleepTime);
    }

    private void writeTag(String oldTag, String newTag, Long sleepTime) throws InterruptedException {
        final WriteTagCmd writeTagCmd = new WriteTagCmd(oldTag, newTag);
        ctx.writeAndFlush(writeTagCmd);
        Thread.sleep(sleepTime);
    }

}
