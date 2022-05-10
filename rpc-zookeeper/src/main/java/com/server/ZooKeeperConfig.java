package com.server;

import lombok.Data;
import org.apache.zookeeper.Watcher;

/**
 * Zookeeper配置
 */
@Data
public class ZooKeeperConfig {
    private String ServicePath = "127.0.0.1:2181";
    private int sessionTimeout = 3*1000;
    private Class<? extends Watcher> monitor = ZookeeperMonitor.class;

}
