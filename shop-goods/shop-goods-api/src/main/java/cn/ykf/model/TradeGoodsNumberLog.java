package cn.ykf.model;

import java.util.Date;

public class TradeGoodsNumberLog extends TradeGoodsNumberLogKey {
    private Integer goodsNumber;

    private Date logTime;

    public TradeGoodsNumberLog() {
    }

    public TradeGoodsNumberLog(Long goodsId, Long orderId, Integer goodsNumber) {
        super(goodsId, orderId);
        this.goodsNumber = goodsNumber;
    }

    public Integer getGoodsNumber() {
        return goodsNumber;
    }

    public void setGoodsNumber(Integer goodsNumber) {
        this.goodsNumber = goodsNumber;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }
}