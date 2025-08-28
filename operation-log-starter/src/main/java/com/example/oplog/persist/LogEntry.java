package com.example.oplog.persist;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 操作日志的数据模型（POJO），LogRecordAspect 渲染完模板后将其构建出来，交给 LogRecordService 写入目标存储
 */


@Setter
@Getter
public class LogEntry {
    private String tenant;
    private String category;
    private String bizNo;
    private boolean success;
    private String content;
    private String detail;
    private String operatorId;
    private String operatorName;
    private String requestId;
    private String traceId;
    private String errorMsg;
    private Instant createdAt;

}
