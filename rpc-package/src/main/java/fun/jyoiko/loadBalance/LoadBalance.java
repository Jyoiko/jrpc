package fun.jyoiko.loadBalance;
import org.apache.commons.collections4.CollectionUtils;
import fun.jyoiko.dto.RpcRequest;

import java.util.List;

public interface LoadBalance {

    /*
    Choose one from the list of existing service addresses list
    */
    default String selectServiceAddress(List<String> serviceList, RpcRequest request){
        if(CollectionUtils.isEmpty(serviceList)){
            return null;
        }
        if(serviceList.size()==1){
            return serviceList.get(0);
        }
        return doSelect(serviceList,request);
    }
    String doSelect(List<String> serviceList, RpcRequest request);
}
