package fun.jyoiko.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ServiceConfig {
    /**
     * 服务的版本号
     */
    private String version;
    /**
     * 对于接口的不同实现，根据group来区分
     */
    private String group;

    private Object service;

    public String getServiceName(){
        String canonicalName = this.service.getClass().getInterfaces()[0].getCanonicalName();
        return canonicalName+this.getGroup()+this.getVersion();
    }
}
