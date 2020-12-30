package cn.ykf.controller;

import cn.ykf.entity.Result;
import cn.ykf.model.TradeOrder;
import cn.ykf.service.OrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单控制器
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @DubboReference(check = false)
    private OrderService orderService;

    @GetMapping
    public Result test() {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setGoodsId(1L);
        return orderService.confirmOrder(tradeOrder);
    }
}
