package fun.jyoiko.transfer.client;

import fun.jyoiko.dto.RpcRequest;

public interface RequestTransport {
    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}