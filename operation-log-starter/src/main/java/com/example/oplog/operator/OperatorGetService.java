package com.example.oplog.operator;

/** 获取“当前操作者”的 SPI */
public interface OperatorGetService {
    Operator getCurrent();
}
