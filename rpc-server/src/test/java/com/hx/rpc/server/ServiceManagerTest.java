package com.hx.rpc.server;

import com.hx.rpc.server.annotation.RPCServiceImpl;
import com.hx.rpc.server.codec.Request;
import com.hx.rpc.server.codec.ServiceDescriptor;
import com.hx.rpc.server.codec.common.utils.ReflectionUtils;
import com.hx.rpc.server.handler.ServiceInstance;
import com.hx.rpc.server.handler.ServiceManager;
import com.hx.rpc.server.handler.ServiceScanUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.Assert.*;

@Slf4j
public class ServiceManagerTest {
    ServiceManager sm;

    @Test
    public void ScanServiceImpl() throws Exception{
        // 要扫描的包
        String packageName = "com.hx.rpc.server.ServiceImpl";
        //反射
        Reflections ref = new Reflections(packageName);
        // 获取扫描到的标记注解的集合
        /*Set<Class<?>> set = ref.getTypesAnnotatedWith(RPCServiceImpl.class);*/
        Set<Class<?>> set = ServiceScanUtil.getAnnotationClasses(packageName, RPCServiceImpl.class);
        log.info("--------start-------------");
        for (Class<?> c : set) {
            // 循环获取标记的注解
            Annotation annotation = c.getAnnotation(RPCServiceImpl.class);
            if(annotation!=null){
                // 打印注解中的内容
                log.info("表名 + {}",c.getName());
            }

        }
        log.info("--------over-------------");
    }


    @Test
    public void register() {
        /*TestInterface bean = new TestClass();
        sm.register(TestInterface.class, bean);*/
        try {
            sm.AutomaticRegister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lookup() {
        Method[] method = ReflectionUtils.getPublicMethods(TestInterface.class);
        ServiceDescriptor sdp = ServiceDescriptor.from(TestInterface.class, method[0]);

        Request request = new Request();
        request.setService(sdp);
        ServiceInstance sis = sm.lookup(request);
        assertNotNull(sis);
    }
}