package fun.jyoiko.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LoadBalanceEnum {
    HASH_LoadBalance("hashLB"),
    RANDOM_LoadBalance("randomLB");
    private final String name;
}
