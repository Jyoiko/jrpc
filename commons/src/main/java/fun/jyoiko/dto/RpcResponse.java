package fun.jyoiko.dto;

import fun.jyoiko.enums.ResponseEnum;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse implements Serializable {

    private String requestId;
    private Integer statusCode;
    private String statusMsg;
    private Object data;

    public static RpcResponse success(Object data, String requestId){
        RpcResponse response = new RpcResponse();
        response.setStatusCode(ResponseEnum.SUCCESS.getCode());
        response.setStatusMsg(ResponseEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (data != null) {
            response.setData(data);
        }
        return response;
    }

    public static RpcResponse fail(){
        RpcResponse response = new RpcResponse();
        response.setStatusCode(ResponseEnum.FAIL.getCode());
        response.setStatusMsg(ResponseEnum.FAIL.getMessage());
        response.setRequestId("error");

        return response;
    }
}
