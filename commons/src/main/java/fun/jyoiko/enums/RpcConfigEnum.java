package fun.jyoiko.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG("jRpc.properties"),
    ZK_ADDRESS("jRpc.zk.address");

    private final String path;
}
