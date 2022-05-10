package com.hx.rpc.service;

import com.hx.rpc.server.annotation.RPCService;

@RPCService
public interface CalcService{
    int add(int a, int b);
    int minus(int a, int b);
}
