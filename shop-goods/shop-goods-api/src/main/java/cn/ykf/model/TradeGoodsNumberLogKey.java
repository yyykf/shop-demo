package cn.ykf.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class TradeGoodsNumberLogKey implements Serializable {
    private Long goodsId;

    private Long orderId;

    private Integer goodsLogType;

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getGoodsLogType() {
        return goodsLogType;
    }

    public void setGoodsLogType(Integer goodsLogType) {
        this.goodsLogType = goodsLogType;
    }
}