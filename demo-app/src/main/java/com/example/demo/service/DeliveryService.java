package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.dal.entity.OrderDO;
import com.example.demo.dal.mapper.OrderMapper;
import com.example.oplog.annotation.LogRecord;
import com.example.oplog.context.LogRecordContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {
    private final OrderMapper orderMapper;

    public DeliveryService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Transactional
    @LogRecord(
            success = "修改了订单配送地址：从“#{#oldAddress}”到“#{#req.address}”",
            fail    = "尝试修改订单(#{#req.orderNo})配送地址失败：原因=#{_errorMsg}",
            bizNo   = "#{#req.orderNo}",
            category= "DELIVERY",
            condition = "!#req.address.equals(#oldAddress)",
            detail = "联系人手机号：{mask{#req.phone}}"
    )
    public void changeAddress(ChangeAddressReq req) {
        OrderDO old = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>()
                .eq(OrderDO::getOrderNo, req.orderNo()));
        String oldAddr = old == null ? "(空)" : (old.getAddress() == null ? "(空)" : old.getAddress());
        LogRecordContext.put("oldAddress", oldAddr);

        int updated = orderMapper.update(null, new LambdaUpdateWrapper<OrderDO>()
                .set(OrderDO::getAddress, req.address())
                .eq(OrderDO::getOrderNo, req.orderNo()));
        if (updated == 0) {
            throw new IllegalStateException("订单不存在或未更新");
        }
    }
}
