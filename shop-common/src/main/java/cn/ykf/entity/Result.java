package cn.ykf.entity;

import cn.ykf.constant.ShopCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 统一响应结果
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Result implements Serializable {
    /** 是否成功标识 */
    private Boolean success;
    /** 响应消息 */
    private String message;
    /** 响应状态码 */
    private Integer code;

    /**
     * 获取响应结果
     *
     * @param code 响应状态码枚举
     * @return Result
     */
    public static Result of(final ShopCode code) {
        return new Result(code.getSuccess(), code.getMessage(), code.getCode());
    }
}
