package cn.ykf.controller;

import cn.ykf.ShopWebApplication;
import cn.ykf.model.TradeOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.Charset;

/**
 * 订单控制器测试类
 */
@Slf4j
@SpringBootTest(classes = ShopWebApplication.class)
public class OrderControllerTest {

    @Resource
    private WebApplicationContext context;

    private MockMvc mvc;

    /** 待确认订单 */
    private TradeOrder order;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();

        order = new TradeOrder();
        order.setGoodsId(1L);
        order.setUserId(345963634385633280L);
        order.setCouponId(2L);
        order.setAddress("广东");
        order.setGoodsNumber(1);
        order.setGoodsPrice(new BigDecimal(5000));
        order.setShippingFee(BigDecimal.ZERO);
        order.setOrderAmount(new BigDecimal(5000));
        order.setMoneyPaid(new BigDecimal(100));
    }

    @Test
    @DisplayName("测试MockMvc")
    void test() throws Exception {
        String result = mvc.perform(MockMvcRequestBuilders.get("/orders"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString(Charset.defaultCharset());

        log.info("响应结果：{}", result);
    }

    @Test
    @DisplayName("测试下单")
    void testConfirmOrder() throws Exception {
        String result = mvc.perform(MockMvcRequestBuilders.post("/orders/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(order)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString(Charset.defaultCharset());

        log.info("响应结果：{}", result);
    }
}