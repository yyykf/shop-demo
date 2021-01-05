package cn.ykf.controller;

import cn.ykf.constant.ShopCode;
import cn.ykf.entity.Result;
import cn.ykf.model.TradePay;
import cn.ykf.service.PayService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付控制器
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2021/1/5
 */
@RestController
@RequestMapping("/pays")
public class PayController {

    @DubboReference(check = false)
    private PayService payService;

    @GetMapping(produces = "application/json;charset=UTF-8")
    public Result test() {
        return Result.of(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
    }

    @PostMapping
    public Result createPayment(@RequestBody TradePay pay) {
        return payService.createPayment(pay);
    }

    @PostMapping("/callback")
    public Result callbackPayment(@RequestBody TradePay pay) {
        return payService.callbackPayment(pay);
    }
}
