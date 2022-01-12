package fun.qianfg.demo.udp.broadcaster;

import fun.qianfg.demo.udp.encoder.LogEventEncoder;
import fun.qianfg.demo.udp.pojo.LogEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

public class LogEventBroadcaster {
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final File file;

    public LogEventBroadcaster(InetSocketAddress address, File file) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)    //设置SO_BROADCAST套接字选项
                .handler(new LogEventEncoder(address));
        this.file = file;
    }

    public void run() throws Exception {
        Channel ch = bootstrap.bind(9999).sync().channel();
        long pointer = 0;

        while (true) {
            long len = file.length();
            if (pointer > len) {
                pointer = len;
            } else if (pointer < len) {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                //设置当前的文件指针，以确保没有任何的旧日志被发送
                raf.seek(pointer);
                String line;
                while ((line = raf.readLine()) != null) {
                    ch.writeAndFlush(new LogEvent(null, -1, file.getName(), line));
                }
                //存储其在文件中的当前位置
                pointer = raf.getFilePointer();
                raf.close();
            }
            //休眠1秒，如果被中断，则退出循环，否则重新处理它
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                Thread.interrupted();
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        group.shutdownGracefully();
    }

    public static void main(String[] args) {
        int sendPort = 9999;
        File file = new File("C:\\Users\\Administrator\\IdeaProjects\\learn_netty\\file02.txt");
        LogEventBroadcaster broadcaster = new LogEventBroadcaster(new InetSocketAddress("172.16.10.142", sendPort), file);

        try {
            broadcaster.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            broadcaster.stop();
        }
    }
}
