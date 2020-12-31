package cn.ykf.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 支付相关vo
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/31
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PayVo implements Serializable {

    private Long payId;

    private Long orderId;
}
