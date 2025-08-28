package com.example.demo.dal.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@TableName("op_log")
public class OpLogDO {
    @TableId
    private Long id;
    private String tenant;
    private String category;
    private String bizNo;
    private String content;
    private String detail;
    private String operatorId;
    private String operatorName;
    private Integer success;
    private String errorMsg;
    private String requestId;
    private String traceId;
    private LocalDateTime createdAt;

}
