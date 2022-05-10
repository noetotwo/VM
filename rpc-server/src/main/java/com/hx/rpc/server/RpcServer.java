package com.hx.rpc.server;

import com.hx.rpc.server.codec.Decoder;
import com.hx.rpc.server.codec.Encoder;
import com.hx.rpc.server.codec.Request;
import com.hx.rpc.server.codec.Response;
import com.hx.rpc.server.codec.common.utils.ReflectionUtils;
import com.hx.rpc.server.transport.RequestHandler;
import com.hx.rpc.server.transport.TransportService;
import com.server.ZooKeeperConfig;
import com.server.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class RpcServer {
    private RpcServerConfig config;
    private TransportService net;
    private Encoder encoder;
    private Decoder decoder;
    private ServiceManager serviceManager;
    private ServiceInvoker serviceInvoker;
    private ZookeeperService zookeeper;

    public RpcServer(RpcServerConfig config) {
        this.config = config;

        //net
        this.net = ReflectionUtils.newInstance(config.getTransportClass());
        this.net.init(config.getPort(), this.handler);

        //codec
        this.encoder = ReflectionUtils.newInstance(config.getEncoderClass());
        this.decoder = ReflectionUtils.newInstance(config.getDecoderClass());

        //zookeeper
        this.zookeeper = new ZookeeperService(new ZooKeeperConfig());

        //service
        this.serviceManager = new ServiceManager(zookeeper,config.getRegistryPath());
        this.serviceInvoker = new ServiceInvoker();
    }

    public <T> void register(Class<T> interfaceClass, T bean) {
        init();
        this.serviceManager.register(interfaceClass, bean);
    }

    public void AutomaticRegister(){
        init();
        try {
            this.serviceManager.AutomaticRegister();
        } catch (Exception e) {
            log.info("自动注册异常");
            e.printStackTrace();
        }
    }

    public void start(){
        this.net.start();
    }

    public void stop(){
        this.net.stop();
    }

    public void init(){
        try {
            if (!zookeeper.exist(config.getRegistryPath())){
                zookeeper.createEphemeraNode(config.getRegistryPath(),"");
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建处理请求的handler
     */
    private RequestHandler handler = new RequestHandler() {
        @Override
        public void onRequest(InputStream recive, OutputStream toResp) {
            Response resp = new Response();
            try {
                //输入流反序列化成一个请求对象
                byte[] inBytes = IOUtils.readFully(recive, recive.available());
                Request request = decoder.decode(inBytes, Request.class);

                log.info("get Request:", request);

                //查找服务，并执行
                ServiceInstance sis = serviceManager.lookup(request);
                Object ret = serviceInvoker.invoker(sis, request);
                //返回结果
                resp.setData(ret);
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                resp.setCode(1);
                resp.setMessage("RpcServer got error:" + e.getClass().getName() + ":" + e.getMessage());
            } finally {
                try {
                    byte[] outBytes = encoder.encode(resp);
                    toResp.write(outBytes);
                    log.info("responsed client");
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
    };
}
