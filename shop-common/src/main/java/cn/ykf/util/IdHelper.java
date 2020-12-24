package cn.ykf.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ID生成器持有类，保存每个类对应的ID生成器
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@Slf4j
public class IdHelper {

    /** 每个类的ID生成器 */
    private static final Map<Class<?>, IdUtil> CACHE = new ConcurrentHashMap<>();

    /**
     * 获取该类的唯一ID
     *
     * @param clazz 指定类
     * @return 唯一ID
     */
    public static Long getNewId(Class<?> clazz) {
        IdUtil idUtil = CACHE.get(clazz);
        if (idUtil == null) {
            idUtil = IdUtil.getInstance();
            // 防止并发情况下使用两个不同的生成器
            IdUtil oldUtil = CACHE.putIfAbsent(clazz, idUtil);
            if (oldUtil != null) {
                idUtil = oldUtil;
            }
        }

        return idUtil.nextId();
    }

    /**
     * 打印所有ID生成器
     */
    public static void printIdUtil() {
        Set<Map.Entry<Class<?>, IdUtil>> entries = CACHE.entrySet();
        for (Map.Entry<Class<?>, IdUtil> entry : entries) {
            log.info("{} <--> {}", entry.getKey(), entry.getValue());
        }
    }
}
