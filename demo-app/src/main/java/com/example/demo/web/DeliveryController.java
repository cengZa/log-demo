package com.example.demo.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.dal.entity.OrderDO;
import com.example.demo.dal.mapper.OrderMapper;
import com.example.demo.service.ChangeAddressReq;
import com.example.demo.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor

public class DeliveryController {
    private final DeliveryService service;
    private final OrderMapper orderMapper;



    @GetMapping("/{orderNo}")
    public Map<String, Object> get(@PathVariable String orderNo) {
        OrderDO obj = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>()
                .eq(OrderDO::getOrderNo, orderNo));
        return Map.of("orderNo", orderNo, "address", obj == null ? "" : obj.getAddress());
    }

    @PostMapping("/{orderNo}/address")
    public Map<String, Object> change(@PathVariable String orderNo, @RequestBody Map<String, String> body) {
        service.changeAddress(new ChangeAddressReq(orderNo, body.get("address"), body.get("phone")));
        return Map.of("ok", true, "orderNo", orderNo, "address", body.get("address"));
    }
}
