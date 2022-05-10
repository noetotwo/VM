package com.hx.rpc.server.ServiceImpl;

import com.hx.rpc.service.TestTemp;
import com.hx.rpc.server.annotation.RPCServiceImpl;

@RPCServiceImpl
public class TestTempImpl implements TestTemp {

    @Override
    public void add() {
        System.out.println("我是Test");
    }
}
