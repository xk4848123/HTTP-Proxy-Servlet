package com.zary.sniffer.agent.core.plugin.loader;

import com.zary.sniffer.agent.core.consts.CoreConsts;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 拦截处理类辅助类加载器
 * 由于探针拦截点拦截的代码可能在不同的classloader中，无法直接找到plugins插件包中的类
 * 所以此类提供全局类加载器缓存和类缓存，为不同classloader提供plugins插件目录下面的包扫描能力
 */
public class InterceptorHandlerClassLoader {
    /**
     * 对象池：每个类在未知classloader中的单实例
     */
    private static final ConcurrentHashMap<String, Object> object_instance_cache = new ConcurrentHashMap<>();
    /**
     * 锁
     */
    private static final ReentrantLock _locker = new ReentrantLock();
    /**
     * 类加载器缓存
     */
    private static final Map<String, ClassLoader> classloader_relation_cache = new HashMap<>();

    /**
     * 加载拦截类对象
     *
     * @param root              插件根目录
     * @param className         类名
     * @param targetClassLoader 当前classloader
     * @param <T>               类class
     */
    public static <T> T load(String root, String className, ClassLoader targetClassLoader)
            throws Exception {
        //未指定classloader默认当前classloader
        if (targetClassLoader == null) {
            targetClassLoader = InterceptorHandlerClassLoader.class.getClassLoader();//基本就是AppClassLoader
        }
        //对象池key
        String keyCache = className + "_OF_" + root + "_OF_" + targetClassLoader.getClass().getName() + "@" + Integer.toHexString(targetClassLoader.hashCode());
        //缓存获取对象
        Object instance = object_instance_cache.get(keyCache);
        if (instance == null) {
            _locker.lock();
            ClassLoader agentLoader;//targetClassLoader关联的AgentClassLoader
            try {
                agentLoader = classloader_relation_cache.get(targetClassLoader + "_OF_" + root);
                if (agentLoader == null) {
                    agentLoader = new AgentClassLoader(targetClassLoader, root, new String[]{CoreConsts.PLUGIN_DIR});
                    classloader_relation_cache.put(targetClassLoader + "_OF_" + root, agentLoader);
                }
            } finally {
                _locker.unlock();
            }
            instance = Class.forName(className, true, agentLoader).newInstance();
            object_instance_cache.put(keyCache, instance);
        }
        return (T) instance;
    }
}
