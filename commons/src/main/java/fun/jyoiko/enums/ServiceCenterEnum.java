package fun.jyoiko.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceCenterEnum {
    SERVICE_DISCOVERY_ENUM("ZK"),
    SERVICE_REGISTRY_ENUM("ZK"),
    SERVICE_PROVIDER_ENUM("ZK");
    private final String name;
}
