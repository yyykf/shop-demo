package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeGoodsMapper;
import cn.ykf.dao.TradeGoodsNumberLogMapper;
import cn.ykf.dao.TradeMqConsumerLogMapper;
import cn.ykf.entity.CancelOrderMsg;
import cn.ykf.entity.Result;
import cn.ykf.entity.TradeMqConsumerLog;
import cn.ykf.entity.TradeMqConsumerLogExample;
import cn.ykf.entity.TradeMqConsumerLogKey;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeGoods;
import cn.ykf.model.TradeGoodsExample;
import cn.ykf.model.TradeGoodsNumberLog;
import cn.ykf.service.GoodsService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * 商品业务实现类
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@Slf4j
@DubboService
public class GoodsServiceImpl implements GoodsService {

    /** 取消订单最大重试次数 */
    private static final Integer CANCEL_ORDER_MSG_MAX_RETRY_TIMES = 3;

    @Resource
    private GoodsService self;

    @Resource
    private TradeGoodsMapper goodsMapper;

    @Resource
    private TradeGoodsNumberLogMapper logMapper;

    @Resource
    private TradeMqConsumerLogMapper consumerLogMapper;

    @Override
    public TradeGoods get(Long goodsId) {
        if (goodsId == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        return goodsMapper.selectByPrimaryKey(goodsId);
    }

    @Override
    public Result reduceGoodsStock(TradeGoodsNumberLog goodsNumberLog) {
        // 校验
        // fixme RPC调用时，直接抛出异常不太好，应该封装为Result返回
        if (goodsNumberLog == null ||
                goodsNumberLog.getGoodsId() == null ||
                goodsNumberLog.getOrderId() == null ||
                goodsNumberLog.getGoodsLogType() == null ||
                goodsNumberLog.getGoodsNumber() == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        // 先查商品
        TradeGoods goods = goodsMapper.selectByPrimaryKey(goodsNumberLog.getGoodsId());
        if (goods == null) {
            BusinessException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }

        // 如果此时的库存不足，那么都不用通过乐观锁来执行sql了
        Integer availableStock = goods.getGoodsNumber();
        Integer needStock = goodsNumberLog.getGoodsNumber();
        if (availableStock < needStock) {
            BusinessException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }

        // 更新库存
        int result = goodsMapper.updateStock(availableStock - needStock, availableStock, goodsNumberLog.getGoodsId());
        // 更新失败说明可用库存被并发修改了
        if (result == 0) {
            BusinessException.cast(ShopCode.SHOP_REDUCE_GOODS_NUM_FAIL);
        }

        // 记录日志
        goodsNumberLog.setLogTime(new Date());
        logMapper.insert(goodsNumberLog);

        return Result.of(ShopCode.SHOP_SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlerCancelOrderMsg(String tags, String msgId, String keys, String body, String consumerGroup) {
        if (StringUtils.isAnyBlank(tags, keys, consumerGroup, msgId, body)) {
            log.error("消息必需参数不完整, tags: {}, keys: {}, consumerGroup: {}, msgId: {}, body: {}", tags, keys,
                    consumerGroup, msgId, body);
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        CancelOrderMsg cancelOrderMsg = JSON.parseObject(body, CancelOrderMsg.class);
        if (cancelOrderMsg == null) {
            log.error("消息体反序列化为空, {}", body);
            BusinessException.cast(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL);
        }

        try {
            // 保存日志
            if (!self.saveConsumerLog(tags, msgId, keys, body, consumerGroup)) {
                return;
            }

            // 回退库存
            self.rollbackGoodsStock(cancelOrderMsg.getGoodsId(), cancelOrderMsg.getGoodsNum(),
                    cancelOrderMsg.getOrderId());

            // 更新消费日志
            self.updateConsumerLogToSuccess(tags, keys, consumerGroup);
            log.info("商品 {} 回退库存成功", cancelOrderMsg.getGoodsId());
        } catch (Exception e) {
            log.error("异常信息", e);

            // 消费失败
            self.updateConsumerLogToFail(tags, keys, consumerGroup);

            log.error("商品 {} 回退库存失败，等待重试", cancelOrderMsg.getGoodsId());
            // 让MQ重新投递
            BusinessException.cast(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL);
        }

    }

    /**
     * {@inheritDoc} 无事务，避免回滚
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateConsumerLogToSuccess(String tags, String keys, String consumerGroup) {
        TradeMqConsumerLog consumerLog = consumerLogMapper.selectByPrimaryKey(new TradeMqConsumerLogKey(consumerGroup
                , tags, keys));
        consumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode());
        consumerLog.setConsumerTimestamp(new Date());

        // todo 这里是否需要引入重试机制？保证更新一定成功？
        consumerLogMapper.updateByPrimaryKeySelective(consumerLog);
    }

    /**
     * {@inheritDoc} 无事务，避免回滚
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateConsumerLogToFail(String tags, String keys, String consumerGroup) {
        TradeMqConsumerLog consumerLog =
                consumerLogMapper.selectByPrimaryKey(new TradeMqConsumerLogKey(consumerGroup, tags, keys));
        if (consumerLog != null) {
            consumerLog.setConsumerTimes(consumerLog.getConsumerTimes() + 1);
            consumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL.getCode());
            consumerLogMapper.updateByPrimaryKeySelective(consumerLog);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackGoodsStock(Long goodsId, Integer goodsNum, Long orderId) {
        if (goodsId == null || goodsNum == null || orderId == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID, "商品id-回退数量-订单id", goodsId, goodsNum, orderId);
        }

        TradeGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null) {
            log.info("待回退库存商品 {} 不存在", goodsId);
            return;
        }
        // 原库存
        Integer oldStock = goods.getGoodsNumber();
        goods.setGoodsNumber(oldStock + goodsNum);
        // 乐观锁更新
        TradeGoodsExample condition = new TradeGoodsExample();
        condition.createCriteria().andGoodsIdEqualTo(goodsId).andGoodsNumberEqualTo(oldStock);
        int result = goodsMapper.updateByExampleSelective(goods, condition);
        if (result == 0) {
            // 回退库存失败
            log.error("回退库存失败，商品 {} 库存被并发更新，需要重试", goodsId);
            BusinessException.cast(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL);
        }

        // 记录库存操作日志
        TradeGoodsNumberLog goodsNumberLog = new TradeGoodsNumberLog(goodsId, orderId,
                ShopCode.SHOP_GOODS_ROLLBACK.getCode(), goodsNum);
        goodsNumberLog.setLogTime(new Date());
        logMapper.insert(goodsNumberLog);
    }

    /**
     * {@inheritDoc} 无事务，避免回滚
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean saveConsumerLog(String tags, String msgId, String keys, String body, String consumerGroup) {
        // 查询消息消费记录
        TradeMqConsumerLog consumerLog = consumerLogMapper.selectByPrimaryKey(new TradeMqConsumerLogKey(consumerGroup
                , tags, keys));

        if (consumerLog != null) {
            // 处理成功或者正在处理，结束
            boolean alreadyProcess = Objects.equals(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode(),
                    consumerLog.getConsumerStatus())
                    || Objects.equals(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode(),
                    consumerLog.getConsumerStatus());
            if (alreadyProcess) {
                log.info("消息 {} 正在处理或已经处理", msgId);
                return false;
            }

            // 消费失败超过3次，不再处理，等待人工修正
            if (consumerLog.getConsumerTimes() > CANCEL_ORDER_MSG_MAX_RETRY_TIMES) {
                log.info("消息 {} 消费失败超过3次，不再消费", msgId);
                return false;
            }

            // 乐观锁更新消费状态
            consumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
            TradeMqConsumerLogExample condition = new TradeMqConsumerLogExample();
            condition.createCriteria()
                    .andGroupNameEqualTo(consumerGroup)
                    .andMsgTagEqualTo(tags)
                    .andMsgKeyEqualTo(keys)
                    .andConsumerTimesEqualTo(consumerLog.getConsumerTimes())
                    .andConsumerStatusEqualTo(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL.getCode());
            int result = consumerLogMapper.updateByExampleSelective(consumerLog, condition);
            if (result == 0) {
                log.info("消息 {} 状态已经被并发修改", msgId);
            }

            return result > 0;
        }

        consumerLog = new TradeMqConsumerLog(consumerGroup, tags, keys, msgId, body,
                ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode(), 0, null, null);
        return consumerLogMapper.insertSelective(consumerLog) > 0;
    }
}
