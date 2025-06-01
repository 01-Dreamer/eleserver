package com.zxylearn.eleserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxylearn.eleserver.pojo.entity.PayOrder;

import java.util.List;

public interface PayOrderService extends IService<PayOrder> {
    boolean addPayOrder(PayOrder payOrder);
    List<PayOrder> getPayOrderByUserId(Integer userId);
    Integer submitPayOrder(Integer userId, String orderId);
}
