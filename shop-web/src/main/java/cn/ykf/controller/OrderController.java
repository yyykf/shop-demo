package cn.ykf.controller;

import cn.ykf.constant.ShopCode;
import cn.ykf.entity.Result;
import cn.ykf.model.TradeOrder;
import cn.ykf.service.OrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping(produces = {"application/json;charset=UTF-8"})
    public Result test() {
        return Result.of(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
    }

    /**
     * 确认订单
     *
     * @param order 待确认订单
     * @return {@code Result}
     */
    @PostMapping("/confirm")
    public Result confirmOrder(@RequestBody TradeOrder order) {
        return orderService.confirmOrder(order);
    }
}
