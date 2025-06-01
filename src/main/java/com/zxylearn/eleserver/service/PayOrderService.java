package com.zxylearn.eleserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxylearn.eleserver.pojo.entity.PayOrder;

public interface PayOrderService extends IService<PayOrder> {
    boolean addPayOrder(PayOrder payOrder);
}
