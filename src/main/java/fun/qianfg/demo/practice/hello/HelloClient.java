package fun.qianfg.demo.practice.hello;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class HelloClient {
    public static void main(String[] args) {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap clientBootStrap = new Bootstrap();
        clientBootStrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
        ChannelFuture channelFuture = null;
        try {
            channelFuture = clientBootStrap.connect(new InetSocketAddress("127.0.0.1", 1234)).sync();
            System.out.println("client start...");
            channelFuture.channel().close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
