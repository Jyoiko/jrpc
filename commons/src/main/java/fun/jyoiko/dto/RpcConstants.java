package fun.jyoiko.dto;

public class RpcConstants {
    public static final int CAFE_BABE=0xCAFEBABE;
    public static final byte VERSION = 1;
    public static final byte HEAD_LENGTH = 16;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final byte ping=0;
    public static final byte pong=1;

    public static final int MAX_LENGTH=8*1024*1024;
}
