package com.hx.rpc.example;

import com.hx.rpc.server.StartCore.RpcServer;
import com.hx.rpc.server.config.RpcServerConfig;

public class Server {
    public static void main(String[] args) {
        RpcServer server = new RpcServer(new RpcServerConfig());
//        server.openNettyService();
        server.openHTTPService();
        //自动注册服务
        server.AutomaticRegister();
//        server.NettyStart();
        server.HTTPStart();
    }

}
