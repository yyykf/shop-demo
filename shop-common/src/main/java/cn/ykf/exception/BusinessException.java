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
}