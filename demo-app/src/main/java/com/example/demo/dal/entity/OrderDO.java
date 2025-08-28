package com.example.demo.dal.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("orders")
public class OrderDO {
    @TableId
    private Long id;
    private String orderNo;
    private String address;
}
