package cn.ykf.service;

import cn.ykf.model.TradeUser;

/**
 * 用户业务接口
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
public interface UserService {
    /**
     * 根据id查询
     *
     * @param userId 用户id
     * @return 对应用户
     */
    TradeUser get(Long userId);
}
