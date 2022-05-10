package com.server;

import com.hx.rpc.server.codec.common.utils.ReflectionUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Data
@Slf4j
public class ZookeeperService {

    ZooKeeper zooKeeper;

    public ZookeeperService(ZooKeeperConfig config) {
        try {
            this.zooKeeper = new ZooKeeper(config.getServicePath(),config.getSessionTimeout(),
                    ReflectionUtils.newInstance(config.getMonitor()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建持久Zookeeper节点
     *
     * @param nodePath  节点路径（如果父节点不存在则会自动创建父节点）
     * @param nodeValue 节点数据
     * @return 返回创建成功的节点路径
     */
    public String createEphemeraNode(String nodePath, String nodeValue) {
        try {
            return zooKeeper.create(nodePath,nodeValue.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        } catch (Exception e) {
            log.error("创建持久Zookeeper节点失败,nodePath:{},nodeValue:{}", nodePath, nodeValue, e);
        }
        return null;
    }

    /**
     * 创建临时Zookeeper节点
     *
     * @param nodePath  节点路径（如果父节点不存在则会自动创建父节点）
     * @param nodeValue 节点数据
     * @return 返回创建成功的节点路径
     */
    public String createPersistentNode(String nodePath, String nodeValue) {
        try {
            return zooKeeper.create(nodePath,nodeValue.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        } catch (Exception e) {
            log.error("创建临时Zookeeper节点失败,nodePath:{},nodeValue:{}", nodePath, nodeValue, e);
        }
        return null;
    }

    /**
     * 创建只读临时Zookeeper节点
     *
     * @param nodePath  节点路径（如果父节点不存在则会自动创建父节点）
     * @param nodeValue 节点数据
     * @return 返回创建成功的节点路径
     */
    public String createReadNode(String nodePath, String nodeValue) {
        try {
            return zooKeeper.create(nodePath,nodeValue.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.READ_ACL_UNSAFE,CreateMode.PERSISTENT);
        } catch (Exception e) {
            log.error("创建临时Zookeeper节点失败,nodePath:{},nodeValue:{}", nodePath, nodeValue, e);
        }
        return null;
    }

    /**
     * 得到Zookeeper节点的data
     * @param nodePath
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String getData(String nodePath) throws KeeperException,InterruptedException {
        return new String(zooKeeper.getData(nodePath,false, null));
    }


    /**
     * 修改Zookeeper节点的data
     * @param nodePath
     * @param NewData
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String setData(String nodePath,String NewData) throws KeeperException,InterruptedException {
        return zooKeeper.setData(nodePath,NewData.getBytes(StandardCharsets.UTF_8),-1).toString();
    }

    /**
     * 删除Zookeeper节点的data
     * @param nodePath
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void DeleteData(String nodePath) throws KeeperException,InterruptedException {
        zooKeeper.delete(nodePath,-1);
    }

    /**
     * 判断节点是否存在
     * @param nodePath
     * @throws KeeperException
     * @throws InterruptedException
     */
    public boolean exist(String nodePath) throws KeeperException,InterruptedException {
        if(zooKeeper.exists(nodePath,false) != null){
            return true;
        }
        return false;
    }
}
