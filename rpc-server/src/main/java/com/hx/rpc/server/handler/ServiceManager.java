package com.hx.rpc.server.handler;

import com.hx.rpc.server.annotation.RPCService;
import com.hx.rpc.server.annotation.RPCServiceImpl;
import com.hx.rpc.server.codec.Request;
import com.hx.rpc.server.codec.ServiceDescriptor;
import com.hx.rpc.server.codec.common.utils.ReflectionUtils;
import com.server.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 管理RPC暴露的服务
 */
@Slf4j
public class ServiceManager {
    private Map<ServiceDescriptor, ServiceInstance> services;
    private ZookeeperService zookeeper;
    private String registryPath;

    public ServiceManager(ZookeeperService service,String path) {
        this.services = new HashMap<>();
        zookeeper = service;
        registryPath = path;
    }

    /**
     * 注册服务
     * @param interfaceClass
     * @param bean
     * @param <T>
     */
    public <T> void register(Class<T> interfaceClass, T bean) {
        Method[] methods = ReflectionUtils.getPublicMethods(interfaceClass);
        for (Method method : methods) {
            ServiceInstance sis = new ServiceInstance(bean, method);
            ServiceDescriptor sdp = ServiceDescriptor.from(interfaceClass, method);

            ZKRegistry(sdp);

            //将提供的具体服务设到map中
            services.put(sdp, sis);
            log.info("register service； {} {}", sdp.getClazz(), sdp.getMethod());
        }
    }

    /**
     * 注册服务
     * @param Class
     * @param bean
     */
    public void ARegister(Class<?> Class,Object bean) {
        Method[] methods = ReflectionUtils.getPublicMethods(Class);
        for (Method method : methods) {
            ServiceInstance sis = new ServiceInstance(bean, method);
            ServiceDescriptor sdp = ServiceDescriptor.from(Class, method);

            log.info("注册的服务 {} ",sdp);
            ZKRegistry(sdp);
            //将提供的具体服务设到map中
            services.put(sdp, sis);

        }
    }

    /**
     * Zookeeper注册服务
     * @param sdp
     */
    public void ZKRegistry(ServiceDescriptor sdp){
        try {
            if(!zookeeper.exist(registryPath+"/"+sdp.getClazz())){
                zookeeper.createPersistentNode(registryPath + "/" + sdp.getClazz(), "");
            }
            if (!zookeeper.exist(registryPath+"/"+sdp.getClazz()+"/"+sdp.hashCode())) {
                zookeeper.createPersistentNode(registryPath + "/" + sdp.getClazz() + "/" + sdp.hashCode(), "");
            }
            zookeeper.createEphemeraNode(registryPath + "/" + sdp.getClazz() + "/" + sdp.hashCode()+"/127.0.0.1:3002", "");
            if(zookeeper.exist(registryPath + "/" + sdp.getClazz() + "/" + sdp.hashCode()+"/127.0.0.1:3002")){
                log.info("zookeeper注册成功 {} ",registryPath + "/" + sdp.getClazz() + "/" + sdp.hashCode()+"/127.0.0.1:3002");
            }

        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            log.info("zookeeper服务注册异常{}",sdp);
        }
    }

    /**
     * 自动注册服务
     * 扫描service包将注解RPCService的类注册
     * 扫描ServiceImpl包将RPCServiceImpl的类注册
     */
    public void AutomaticRegister() throws Exception{
        Set<Class<?>> ServerSet = ServiceScanUtil.getAnnotationClasses("com.hx.rpc.service", RPCService.class);
        Set<Class<?>> ImplSet = ServiceScanUtil.getAnnotationClasses("com.hx.rpc.server.ServiceImpl", RPCServiceImpl.class);
        log.info(String.valueOf(ServerSet));
        log.info(String.valueOf(ImplSet));
        for(Class<?> clazz : ServerSet){
            for (Class<?> impl : ImplSet){
                if(clazz.isAssignableFrom(impl)){
                    ARegister(clazz,impl.newInstance());
                }
            }
        }
    }


    /**
     * 查找服务
     * @param request
     * @return
     */
    public ServiceInstance lookup(Request request) {
        //根据服务的描述从map中查具体服务
        ServiceDescriptor sdp = request.getService();
        return services.get(sdp);
    }
}
