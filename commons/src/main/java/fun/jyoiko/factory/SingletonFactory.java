package fun.jyoiko.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SingletonFactory {

    private static final Map<String, Object> objects=new ConcurrentHashMap<>();
    private SingletonFactory(){}

    public static <T> T getInstance(Class<T> c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        String key=c.toString();
        if(objects.containsKey(key)){
            return c.cast(objects.get(key));
        }else{
            return c.cast(objects.computeIfAbsent(key,k->{
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }));
        }
    }
}
