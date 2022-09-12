package com.hx.rpc.server.config;

import com.hx.rpc.server.codec.Decoder;
import com.hx.rpc.server.codec.Encoder;
import com.hx.rpc.server.codec.JSONDecoder;
import com.hx.rpc.server.codec.JSONEncoder;
import com.hx.rpc.server.netty.server.NettyRpcServer;
import com.hx.rpc.server.transport.HTTPTransportServer;
import com.hx.rpc.server.transport.TransportService;
import lombok.Data;

/**
 * server配置
 */
@Data
public class RpcServerConfig {
    private Class<NettyRpcServer> nettyRpcServer = NettyRpcServer.class;
    private Class<? extends TransportService> transportClass = HTTPTransportServer.class;
    private Class<? extends Encoder> encoderClass = JSONEncoder.class;
    private Class<? extends Decoder> decoderClass = JSONDecoder.class;
    private String registryPath  = "/registry";
    private int port = 3002;
}
