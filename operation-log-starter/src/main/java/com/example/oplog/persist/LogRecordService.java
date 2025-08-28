package com.example.oplog.persist;

/**
 * 持久化SPI，在业务侧提供实现，Starter的默认实现只写到Logger
 */
public interface LogRecordService {
    void record(LogEntry entry);
}
