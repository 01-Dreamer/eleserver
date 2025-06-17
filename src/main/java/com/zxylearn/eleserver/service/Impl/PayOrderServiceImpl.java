package com.zxylearn.eleserver.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxylearn.eleserver.mapper.PayOrderMapper;

import com.zxylearn.eleserver.pojo.entity.PayOrder;
import com.zxylearn.eleserver.service.PayOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayOrderServiceImpl
        extends ServiceImpl<PayOrderMapper, PayOrder>
        implements PayOrderService {

    @Override
    public boolean addPayOrder(PayOrder payOrder) {
        if (payOrder == null) {
            return false;
        }
        return baseMapper.insert(payOrder) > 0;
    }

    @Override
    public List<PayOrder> getPayOrderByUserId(Integer userId) {
        QueryWrapper<PayOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .eq("userId", userId))
                .orderByAsc("createTime");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Integer submitPayOrder(Integer userId, String orderId) {
        PayOrder payOrder = baseMapper.selectOne(new QueryWrapper<PayOrder>()
                .eq("orderId", orderId));

        if (payOrder == null || payOrder.isPay() || !payOrder.getUserId().equals(userId)) {
            return null;
        }

        payOrder.setPay(true);
        if (baseMapper.updateById(payOrder) > 0) {
            return payOrder.getBusinessId();
        }
        return null;
    }

    @Override
    public Integer submitReview(Integer userId, String orderId, Integer review) {
        PayOrder payOrder = baseMapper.selectOne(new QueryWrapper<PayOrder>()
                .eq("orderId", orderId));

        if (payOrder == null || !payOrder.isPay() || !payOrder.getUserId().equals(userId)) {
            return null;
        }

        payOrder.setReview(review);
        if (baseMapper.updateById(payOrder) > 0) {
            return payOrder.getBusinessId();
        }
        return null;
    }
}
