package cn.ykf.exception;

import cn.ykf.constant.ShopCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务自定义异常
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/23
 */
@Slf4j
public class BusinessException extends RuntimeException {

    private ShopCode code;

    private BusinessException(ShopCode code) {
        this.code = code;
    }

    /**
     * 将状态码转换为业务异常
     *
     * @param code 状态码
     */
    public static void cast(ShopCode code) {
        log.error("{}", code);
        throw new BusinessException(code);
    }

    /**
     * 将状态码转换为业务异常
     *
     * @param code 状态码
     * @param desc 额外描述信息
     * @param args 上下文参数
     */
    public static void cast(ShopCode code, String desc, Object... args) {
        log.error("{}, desc: {}, args: {}", code, desc, args);
        throw new BusinessException(code);
    }
}