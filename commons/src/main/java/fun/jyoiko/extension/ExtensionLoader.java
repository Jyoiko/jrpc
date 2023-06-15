package fun.jyoiko.extension;

import fun.jyoiko.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对getExtensionClasses以及cachedClasses存疑，感觉只能存放一个接口的实现类，
 * 但是因为cachedClasses只在null时更新数据，也就是只更新一次，应该是需要把所有类的所有实现都加载
 * 但是loadDir中的
 * String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
 * 又让我觉得只会加载指定的文件（也就是接口）的实现
 *
 * 和SingletonFactory的区别：ExtensionLoader返回的classType一定是接口，因此只能使用接口定义的方法，
 * 如果要使用实现类独有的方法，就要用SingletonFactory，两者返回的引用类型是不同的
 * @param <T>
 */
@Slf4j
public class ExtensionLoader<T> {
    private static final String SERVICE_DIR="META-INF/extensions/";

    private static final Map<Class<?>,ExtensionLoader<?>> EXTENSION_LOADER_MAP= new ConcurrentHashMap<>();/*存放不同 类 的loader（不是classloader，只是ExtensionLoader）*/
    private static final Map<Class<?>,Object> EXTENSION_INSTANCES=new ConcurrentHashMap<>();
    private final Class<?> type;
    //注意这里不是static 这很重要！！！
    private final Map<String,Holder<Object>> cachedInstances=new ConcurrentHashMap<>();/*存放不同 类 的实例，也就是说一个类只对应一个实例？*/
    private final Holder<Map<String,Class<?>>> cachedClasses=new Holder<>();/*这个集合中的元素是经常会变的，
                                  因为这里只会存放一个接口的所有实现，如果要获取不同的接口的实现类，这个集合就会被更新掉*/

    private ExtensionLoader(Class<?> type){this.type=type;}

    /**
     * 可以认为是唯一对外开放的接口，获取loader，
     * 存放在EXTENSION_LOADER_MAP，保证每个class类别（T)只有一个loader
     * @param type
     * @return
     * @param <T>
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type){
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        ExtensionLoader<T> extensionLoader= (ExtensionLoader<T>) EXTENSION_LOADER_MAP.get(type);
        if(extensionLoader==null){
            EXTENSION_LOADER_MAP.putIfAbsent(type,new ExtensionLoader<T>(type));
            extensionLoader=(ExtensionLoader<T>) EXTENSION_LOADER_MAP.get(type);
        }
        return extensionLoader;
    }

    public T getExtension(String name){
        if(StringUtil.isBlank(name)){
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        Holder<Object> holder=cachedInstances.get(name);
        if (holder==null){
            cachedInstances.putIfAbsent(name,new Holder<>());
            holder=cachedInstances.get(name);
        }
        Object instance = holder.getVal();
        if(instance==null){
            synchronized (holder){
                instance=holder.getVal();
                if(instance==null){
                    instance=createExtension(name);
                    holder.setVal(instance);
                }
            }
        }
        return (T)instance;
    }

    /**
     * 根据name找到对应的类（这个过程中会加载所有类，但是不会初始化），通过loadResource可以看到name是怎么确定的，其实就是全路径类名的别名
     * 在这个方法中完成特定类实例的初始化（newInstance）
     * 用EXTENSION_INSTANCES存放初始化过的实例，保证单例
     * @param name
     * @return
     */
    private T createExtension(String name){
        // load all extension classes of type T from file and get specific one by name
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T)EXTENSION_INSTANCES.get(clazz);
        if(instance==null){
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz,clazz.newInstance());
                instance=(T)EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error("createExtension:---"+e.getMessage());
            }
        }
        return instance;
    }

    /**
     * class是类名
     * Holder是对类集合的一个简单封装
     * Holder中的实际内容是一个HashMap,包含了所以加载的类
     * @return
     */
    private Map<String,Class<?>> getExtensionClasses(){
        Map<String, Class<?>> classes = cachedClasses.getVal();
        if(classes==null){
            synchronized (cachedClasses){
                classes=cachedClasses.getVal();
                if(classes==null){
                    classes=new HashMap<>();
                    //load all extensions from dir
                    loadDir(classes);
                    cachedClasses.setVal(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 通过Service_dir以及对应的类名确定需要加载的类，有些接口只有一个实现类，就其实只加载一个类
     * @param classMap
     */
    private void loadDir(Map<String,Class<?>> classMap){
        String fileName = ExtensionLoader.SERVICE_DIR + type.getName();
        Enumeration<URL> urls;
        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        try {
            urls=classLoader.getResources(fileName);
            if(urls!=null){
                while (urls.hasMoreElements()){
                    URL url = urls.nextElement();
                    loadResource(classMap,classLoader,url);
                }
            }
        } catch (IOException e) {
            log.error("loadDir:---"+e.getMessage());

        }
    }

    /**
     * 实际加载类（loadClass）的方法
     * @param classMap
     * @param classLoader
     * @param url
     */
    private void loadResource(Map<String,Class<?>> classMap, ClassLoader classLoader, URL url){
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))){
            String line;
            while ((line = reader.readLine()) != null) {
                int sharp=line.indexOf('#');
                if(sharp>0){
                    // string after # is comment so we ignore it
                    line=line.substring(0,sharp);
                }
                line=line.trim();
                if(line.length()>0){
                    int eq=line.indexOf('=');
                    String name = line.substring(0, eq).trim();
                    String className = line.substring(eq + 1).trim();
                    if(name.length()>0&&className.length()>0){
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            classMap.put(name,clazz);
                        } catch (ClassNotFoundException e) {
                            log.error(e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("loadResource:---"+e.getMessage());
        }
    }
}
