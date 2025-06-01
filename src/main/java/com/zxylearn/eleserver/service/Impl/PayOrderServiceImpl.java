package com.zxylearn.eleserver.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxylearn.eleserver.mapper.PayOrderMapper;

import com.zxylearn.eleserver.pojo.entity.PayOrder;
import com.zxylearn.eleserver.service.PayOrderService;
import org.springframework.stereotype.Service;

@Service
public class PayOrderServiceImpl
        extends ServiceImpl<PayOrderMapper, PayOrder>
        implements PayOrderService {

    @Override
    public boolean addPayOrder(PayOrder payOrder) {
        if(payOrder == null) {
            return false;
        }
        return baseMapper.insert(payOrder) > 0;
    }
}
