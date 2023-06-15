package fun.jyoiko.transfer.client;

import fun.jyoiko.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse>> unprocessedMap=new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse> future){
        unprocessedMap.put(requestId,future);
    }

    public void complete(RpcResponse rpcResponse){
        CompletableFuture<RpcResponse> future = unprocessedMap.remove(rpcResponse.getRequestId());
        if (future!=null){
            future.complete(rpcResponse);
        }else {
            throw new IllegalStateException();
        }
    }
}
