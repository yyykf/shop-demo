package cn.ykf.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 取消订单消息
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/29
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class CancelOrderMsg {
    /** 订单ID */
    private Long orderId;
    /** 优惠券ID */
    private Long couponId;
    /** 用户ID */
    private Long userId;
    /** 使用余额 */
    private BigDecimal userMoney;
    /** 商品ID */
    private Long goodsId;
    /** 商品数量 */
    private Integer goodsNum;

}
