package cn.ykf.listener;

import cn.ykf.entity.Result;
import cn.ykf.model.vo.PayVo;
import cn.ykf.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import javax.annotation.Resource;

/**
 * 事务监听器
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/31
 */
@Slf4j
@RocketMQTransactionListener(corePoolSize = 20, maximumPoolSize = 40)
public class TransactionListener implements RocketMQLocalTransactionListener {

    @Resource
    private PayService payService;

    /**
     * 执行本地事务
     *
     * @param msg   发送成功的half消息
     * @param payId 支付订单id
     * @return {@link RocketMQLocalTransactionState}
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object payId) {
        Long id = (Long) payId;
        if (id == null) {
            log.error("支付订单id为空");
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        // 将订单修改为已支付
        Result result = payService.updatePaymentToSuccess(id);
        if (!result.getSuccess()) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        return RocketMQLocalTransactionState.COMMIT;
    }

    /**
     * 回查本地事务
     *
     * @param msg 待回查的half消息
     * @return {@link RocketMQLocalTransactionState}
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        PayVo vo = (PayVo) msg.getPayload();
        if (vo.getPayId() == null) {
            log.error("待回查支付订单id为空");
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        boolean result = payService.checkPayIsPaid(vo.getPayId());
        if (!result) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        return RocketMQLocalTransactionState.COMMIT;
    }
}
