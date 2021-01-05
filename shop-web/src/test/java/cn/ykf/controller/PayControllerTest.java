package cn.ykf.controller;

import cn.ykf.ShopWebApplication;
import cn.ykf.constant.ShopCode;
import cn.ykf.model.TradePay;
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

@Slf4j
@SpringBootTest(classes = ShopWebApplication.class)
public class PayControllerTest {

    @Resource
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("测试MockMvc")
    void test() throws Exception {
        String result = mvc.perform(MockMvcRequestBuilders.get("/pays"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString(Charset.defaultCharset());

        log.info("响应结果：{}", result);
    }

    @Test
    @DisplayName("测试创建订单")
    void testCreatePayment() throws Exception {
        TradePay pay = new TradePay();
        pay.setOrderId(543865530256334848L);
        pay.setPayAmount(BigDecimal.valueOf(4880L));

        String result = mvc.perform(MockMvcRequestBuilders.post("/pays")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(pay)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString(Charset.defaultCharset());

        log.info("响应结果：{}", result);
    }

    @Test
    @DisplayName("测试支付回调")
    void testCallbackPayment() throws Exception {
        TradePay pay = new TradePay();
        pay.setOrderId(543865530256334848L);
        pay.setPayId(543876251778174976L);
        pay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());

        String result = mvc.perform(MockMvcRequestBuilders.post("/pays/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(pay)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString(Charset.defaultCharset());

        log.info("响应结果：{}", result);
    }
}