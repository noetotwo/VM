package com.hx.rpc.server.StartCore;

import com.hx.rpc.server.handler.ServiceInstance;
import com.hx.rpc.server.handler.ServiceInvoker;
import com.hx.rpc.server.handler.ServiceManager;
import com.hx.rpc.server.codec.Decoder;
import com.hx.rpc.server.codec.Encoder;
import com.hx.rpc.server.codec.Request;
import com.hx.rpc.server.codec.Response;
import com.hx.rpc.server.codec.common.utils.ReflectionUtils;
import com.hx.rpc.server.config.RpcServerConfig;
import com.hx.rpc.server.netty.RequestHandler.RpcRequestHandler;
import com.hx.rpc.server.netty.server.NettyRpcServer;
import com.hx.rpc.server.netty.server.NettyRpcServerHandler;
import com.hx.rpc.server.transport.RequestHandler;
import com.hx.rpc.server.transport.TransportService;
import com.server.ZooKeeperConfig;
import com.server.ZookeeperService;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class RpcServer {
    private  RpcServerConfig config;
    private  TransportService net;
    private NettyRpcServer ser;
    private  Encoder encoder;
    private  Decoder decoder;
    private  ServiceManager serviceManager;
    private  ServiceInvoker serviceInvoker;
    private  ZookeeperService zookeeper;

    public RpcServer(RpcServerConfig config){
        this.config = config;
    }


    /**
     * 配置Netty的运行，支持TCP通讯
     */
    public void openNettyService(){
        this.ser =  ReflectionUtils.newInstance(config.getNettyRpcServer());
        ser.init(config.getPort(),this.nettyHandler);

       //zookeeper
       this.zookeeper = new ZookeeperService(new ZooKeeperConfig());

       //service
       this.serviceManager = new ServiceManager(zookeeper,config.getRegistryPath());
       this.serviceInvoker = new ServiceInvoker();
   }

    /**
     * 配置Jetty的运行，支持Http通讯
     */
    public void openHTTPService(){

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

    public void AllStart(){
        HTTPStart();
        NettyStart();
    }

    /**
     * 启动jetty服务器
     */
    public void HTTPStart(){
        if(this.net == null){
            throw new NullPointerException();
        }
        this.net.start();
    }

    /**
     * 启动Netty服务器
     */
    public void NettyStart(){
        if(this.ser == null){
            throw new NullPointerException();
        }
        this.ser.start();
    }

    public void HTTPStop(){
        this.net.stop();
    }


    public void init(){
        try {
            if (!zookeeper.exist(config.getRegistryPath())){
                zookeeper.createPersistentNode(config.getRegistryPath(),"");
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * 创建处理请求的nettyHandler
     */
    private RpcRequestHandler nettyHandler = new RpcRequestHandler() {
        @Override
        public Response onRequest(Request request) {
            Response resp = new Response();
            //查找服务，并执行
            ServiceInstance sis = serviceManager.lookup(request);
            Object ret = serviceInvoker.invoker(sis, request);
            //返回结果
            resp.setRequestId(request.getRequestId());
            resp.setData(ret);
            return resp;

        }
    };

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

                if(request != null){
                    //查找服务，并执行
                    ServiceInstance sis = serviceManager.lookup(request);
                    if(sis != null) {
                        Object ret = serviceInvoker.invoker(sis, request);
                        //返回结果
                        resp.setData(ret);
                    }else{
                        log.debug("请求服务不存在 request {}",request);
                        resp.setMessage("请求服务不存在");
                    }
                }else{
                    log.debug("请求服务为空 request {}",request);
                    resp.setMessage("请求服务为空");
                }
            } catch (IOException e) {
               /* log.warn(e.getMessage(), e);*/
                resp.setCode(1);
                resp.setMessage("RpcServer got error:" + e.getMessage());
            } finally {
                try {
                    byte[] outBytes = encoder.encode(resp);
                    toResp.write(outBytes);
                    log.info("responsed client");
                } catch (IOException e) {
//                    log.warn(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
    };
}
