package com.example.oplog.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 默认实现——把日志写到 businessLog（logback 里可单独落文件）
 */
public class DefaultLogRecordService implements LogRecordService {
    private static final Logger log = LoggerFactory.getLogger("businessLog");

    @Override
    public void record(LogEntry e) {
        log.info("[oplog] tenant={}, category={}, bizNo={}, success={}, operator={}({}), content={}, detail={}, error={}",
                e.getTenant(), e.getCategory(), e.getBizNo(), e.isSuccess(),
                e.getOperatorId(), e.getOperatorName(), e.getContent(), e.getDetail(), e.getErrorMsg());
    }
}
