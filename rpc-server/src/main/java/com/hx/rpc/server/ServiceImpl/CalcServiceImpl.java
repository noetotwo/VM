package com.hx.rpc.server.ServiceImpl;

import com.hx.rpc.server.annotation.RPCServiceImpl;
import com.hx.rpc.service.CalcService;

@RPCServiceImpl
public class CalcServiceImpl implements CalcService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int minus(int a, int b) {
        return a - b;
    }
}
