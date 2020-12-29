package cn.ykf.entity;

import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
public class TradeMqConsumerLog extends TradeMqConsumerLogKey {
    private String msgId;

    private String msgBody;

    private Integer consumerStatus;

    private Integer consumerTimes;

    private Date consumerTimestamp;

    private String remark;

    public TradeMqConsumerLog(String groupName, String msgTag, String msgKey, String msgId, String msgBody, Integer consumerStatus, Integer consumerTimes, Date consumerTimestamp, String remark) {
        super(groupName, msgTag, msgKey);
        this.msgId = msgId;
        this.msgBody = msgBody;
        this.consumerStatus = consumerStatus;
        this.consumerTimes = consumerTimes;
        this.consumerTimestamp = consumerTimestamp;
        this.remark = remark;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId == null ? null : msgId.trim();
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody == null ? null : msgBody.trim();
    }

    public Integer getConsumerStatus() {
        return consumerStatus;
    }

    public void setConsumerStatus(Integer consumerStatus) {
        this.consumerStatus = consumerStatus;
    }

    public Integer getConsumerTimes() {
        return consumerTimes;
    }

    public void setConsumerTimes(Integer consumerTimes) {
        this.consumerTimes = consumerTimes;
    }

    public Date getConsumerTimestamp() {
        return consumerTimestamp;
    }

    public void setConsumerTimestamp(Date consumerTimestamp) {
        this.consumerTimestamp = consumerTimestamp;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}