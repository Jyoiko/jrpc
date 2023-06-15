package fun.jyoiko.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializerEnum {
    KRYO((byte)0x01,"kryo");

    private final byte code;
    private final String name;

    public static String getNameFromCode(byte code){
        for (SerializerEnum c : SerializerEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
