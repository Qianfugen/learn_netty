package fun.qianfg.demo.broadcast.core;


import fun.qianfg.demo.broadcast.Request.RequestMessage;
import fun.qianfg.demo.broadcast.Response.ResponseMessage;
import fun.qianfg.demo.broadcast.codec.MessageDecoder;
import fun.qianfg.demo.broadcast.codec.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.util.concurrent.DefaultPromise;

import java.net.InetSocketAddress;

public class NettyUdpClient {
    private int sendPort;
    private int listenPort;
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    public NettyUdpClient(Integer sendPort, Integer listenPort) {
        this.sendPort = sendPort;
        this.listenPort = listenPort;
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addFirst(new DatagramPacketEncoder<RequestMessage>(new MessageEncoder()));
                        pipeline.addLast(new DatagramPacketDecoder(new MessageDecoder()));
                        pipeline.addLast(new ClientHandler());
                    }
                });
    }

    public ResponseFuture<ResponseMessage> broadcast(RequestMessage request) {
        ChannelFuture future = null;
        try {
            future = bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }


        ResponseFuture<ResponseMessage> responseFuture = new ResponseFuture<>(new DefaultPromise<>(new DefaultEventLoop()));
        RequestHolder.udpFuture = responseFuture;

        DefaultAddressedEnvelope<RequestMessage, InetSocketAddress> udpRequest = new DefaultAddressedEnvelope<>(request, new InetSocketAddress("255.255.255.255", sendPort));
        future.channel().writeAndFlush(udpRequest);
        return responseFuture;
    }


    public void destroy() {
        eventLoopGroup.shutdownGracefully();
        RequestHolder.REQUEST_MAP.clear();
        RequestHolder.udpFuture = null;
    }
}
