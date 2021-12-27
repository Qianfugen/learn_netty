package fun.qianfg.chapter10.encoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Function: Server端, 可以通过sokit等工具连接测试
 *
 * @author qianfg
 * @date 2021/12/27 20:53
 * @Email: 287541326@qq.com
 */
public class Server {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap;
        try {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(1234)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline()
                                    .addLast(new SafeByteToMessageDecoder())
                                    .addLast(new ToIntegerDecoder())
//                                    .addLast(new ToInteger2Decoder())
                                    .addLast(new IntegerToStringDecoder());

                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("Server Start...");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
