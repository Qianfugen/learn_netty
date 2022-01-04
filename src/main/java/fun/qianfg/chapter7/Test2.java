package fun.qianfg.chapter7;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Function:
 *
 * @author qianfg
 * @date 2022/1/2 16:32
 * @Email: 287541326@qq.com
 */
public class Test2 {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(1234))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) {
                            //do nothing, just test
                        }
                    });
            ChannelFuture future = b.bind().sync();
            System.out.println("NettyServer start...");
            Channel ch = future.channel();
            //使用 EventLoop 调度任务, 10s后执行任务，只执行一次
//            ch.eventLoop().schedule(() -> System.out.println("10 seconds later"), 10, TimeUnit.SECONDS);
            //使用 EventLoop 调度任务, 10s后任务， 每隔10s后执行任务
            ch.eventLoop().scheduleAtFixedRate(() -> System.out.println("10 seconds later"), 10, 10, TimeUnit.SECONDS);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
