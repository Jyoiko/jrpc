package fun.jyoiko.loadBalance;

import fun.jyoiko.dto.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConsistentHashImpl implements LoadBalance{

    private final Map<String,ConsistentHashSelector> selectorMap=new ConcurrentHashMap<>();
    /**
    这里提供不同服务的也会放在一起吗，还是因为只是rpc所以没有区分

    对于不同的提供服务的服务器集合，会有不同的identityHashCode

     相同参数的请求总是发到同一台服务器处理,那参数不同，就可能被分配到不同的服务器
     */
    @Override
    public String doSelect(List<String> serviceList, RpcRequest request) {
        int identityHashCode = System.identityHashCode(serviceList);
        String serviceName = request.getRpcServiceName();
        ConsistentHashSelector selector = selectorMap.get(serviceName);
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectorMap.put(serviceName,new ConsistentHashSelector(identityHashCode,160,serviceList));
            selector=selectorMap.get(serviceName);
        }
        return selector.select(serviceName+ Arrays.stream(request.getParameters()));
    }

    static class ConsistentHashSelector{
        private final int identityHashCode;

        private final TreeMap<Long,String> virtualInvokers=new TreeMap<>();//存储虚拟节点

        /**
         *
         * @param identityHashCode：pass
         * @param replicaNum:应该是每个实际节点invoker创建的虚拟节点数
         * @param invokers:实际的服务节点数
         */
        ConsistentHashSelector(int identityHashCode, int replicaNum, List<String> invokers) {
            this.identityHashCode = identityHashCode;
            for (String invoker :
                    invokers) {
                for (int i = 0; i < replicaNum/4; i++) {
                    byte[] bytes = md5(invoker + i);
                    for(int j=0;j<4;j++){
                        long hash = hash(bytes, j);
                        virtualInvokers.put(hash,invoker);
                    }
                }
            }
        }
        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }
        static byte[] md5(String key){
            MessageDigest md;
            try {
                md=MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return md.digest();
        }

        public String selectKey(long hashCode){
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            if (entry == null) {
                entry=virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }

        public String select(String serviceKey){
            byte[] bytes = md5(serviceKey);
            return selectKey(hash(bytes,0));
        }
    }
}
