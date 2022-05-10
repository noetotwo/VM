package com.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

@Slf4j
public class ZookeeperMonitor implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        log.info("ZNode事件信息改变 --- {} " + event.getState());
    }
}
