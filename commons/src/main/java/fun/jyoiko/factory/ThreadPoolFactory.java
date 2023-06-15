package fun.jyoiko.factory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class ThreadPoolFactory {
    /**
     * 通过 threadNamePrefix 来区分不同线程池（我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）。
     * key: threadNamePrefix
     * value: threadPool
     */
    private static final Map<String, Executor> ThreadPool=new ConcurrentHashMap<>();

    private ThreadPoolFactory(){}

    public static ThreadFactory createThreadFactory(String prefix,Boolean daemon){
        if (prefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setDaemon(daemon)
                        .setNameFormat(prefix+"-%d")
                        .build();
            }else {
                return new ThreadFactoryBuilder().setNameFormat(prefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    public static void shutdownAll(){
        log.info("call shutDownAllThreadPool method");

    }
}
