package com.hx.rpc.server.netty.server;

import com.hx.rpc.server.netty.RequestHandler.RpcRequestHandler;

import com.hx.rpc.server.netty.rpcCode.DelimiterBasedFrameEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * Server. Receive the client message, call the corresponding method according to the client message,
 * and then return the result to the client.
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 16:42:00
 */
@Slf4j
public class NettyRpcServer {

    private String host;
    private int port;
    private RpcRequestHandler handler;
    private String delimiter = "_$";

    @SneakyThrows
    public void init(int port, RpcRequestHandler handler) {
        this.port = port;
        this.host = InetAddress.getLocalHost().getHostAddress();;
        this.handler = handler;
    }

    @SneakyThrows
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(0, 0, 3, TimeUnit.SECONDS));
                            //实现100秒钟，如果两端，如果数据读取，直接断开连接
                            p.addLast(new ReadTimeoutHandler(100));

                            //实现100秒钟，如果两端，如果数据写入，直接断开连接
                            p.addLast(new WriteTimeoutHandler(100));
                            p.addLast(new DelimiterBasedFrameDecoder(2 * 1024, Unpooled.wrappedBuffer(delimiter.getBytes())));
                            p.addLast(new StringEncoder());
                            p.addLast(new StringDecoder());
                            ch.pipeline().addLast(new DelimiterBasedFrameEncoder(delimiter));
                            p.addLast(new NettyRpcServerHandler(handler));
                        }
                    });

            // 绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind("127.0.0.1", port).sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
