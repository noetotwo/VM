package com.hx.rpc.example;

import com.hx.rpc.server.RpcServer;
import com.hx.rpc.server.RpcServerConfig;

public class Server {
    public static void main(String[] args) {
        RpcServer server = new RpcServer(new RpcServerConfig());
        //自动注册服务
        server.AutomaticRegister();
        server.start();
    }

}
