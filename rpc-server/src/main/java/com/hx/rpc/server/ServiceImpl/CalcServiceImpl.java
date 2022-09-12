package com.hx.rpc.server.ServiceImpl;

import com.hx.rpc.server.annotation.RPCServiceImpl;
import com.hx.rpc.service.CalcService;

import java.util.HashMap;

@RPCServiceImpl
public class CalcServiceImpl implements CalcService {
    @Override
    public int add(int a, int b) {
        String s = String.valueOf(a)+String.valueOf(b);
        if (s.length()==0) return 0;
        HashMap<Character, Integer> map = new HashMap<Character, Integer>();
        int max = 0;
        int left = 0;
        for(int i = 0; i < s.length(); i ++){
            if(map.containsKey(s.charAt(i))){
                left = Math.max(left,map.get(s.charAt(i)) + 1);
            }
            map.put(s.charAt(i),i);
            max = Math.max(max,i-left+1);
        }
        return a + b;
    }

    @Override
    public int minus(int a, int b) {
        return a - b;
    }
}
