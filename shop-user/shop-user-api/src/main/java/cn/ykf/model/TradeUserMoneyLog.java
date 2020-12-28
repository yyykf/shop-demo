package cn.ykf.model;

import java.math.BigDecimal;
import java.util.Date;

public class TradeUserMoneyLog extends TradeUserMoneyLogKey {
    private BigDecimal useMoney;

    private Date createTime;

    public TradeUserMoneyLog(BigDecimal useMoney) {
        this.useMoney = useMoney;
    }

    public TradeUserMoneyLog(Long userId, Long orderId, Integer moneyLogType, BigDecimal useMoney) {
        super(userId, orderId, moneyLogType);
        this.useMoney = useMoney;
    }

    public BigDecimal getUseMoney() {
        return useMoney;
    }

    public void setUseMoney(BigDecimal useMoney) {
        this.useMoney = useMoney;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}