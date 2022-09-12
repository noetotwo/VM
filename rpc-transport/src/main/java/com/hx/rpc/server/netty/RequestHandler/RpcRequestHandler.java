package com.hx.rpc.server.netty.RequestHandler;

import com.hx.rpc.server.codec.Request;
import com.hx.rpc.server.codec.Response;


/**
 * RpcRequest processor
 *
 * @author shuang.kou
 * @createTime 2020年05月13日 09:05:00
 */
public interface RpcRequestHandler {
    public Response onRequest(Request request);
}
