package com.example.demo.infrastructure;

import com.example.demo.dal.entity.OpLogDO;
import com.example.demo.dal.mapper.OpLogMapper;
import com.example.oplog.persist.LogEntry;
import com.example.oplog.persist.LogRecordService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class MyBatisPlusLogRecordService implements LogRecordService {
    private final OpLogMapper mapper;

    public MyBatisPlusLogRecordService(OpLogMapper mapper) {
        this.mapper = mapper;
    }

    private static String toJsonOrNull(String s){
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return "";
        boolean looksJson = t.startsWith("{") || t.startsWith("[")
                || t.equals("null") || t.equals("true") || t.equals("false")
                || t.matches("^-?\\d+(?:\\.\\d+)?$")
                || (t.startsWith("\"") && t.endsWith("\""));
        if (looksJson) return t;                             // 已是 JSON => 原样
        String escaped = t.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";                        // 否则当作 JSON 字符串
    }


    @Override
    public void record(LogEntry e) {
        OpLogDO d = new OpLogDO();
        d.setTenant(nvl(e.getTenant(), "default"));
        d.setCategory(e.getCategory());
        d.setBizNo(e.getBizNo());
        d.setContent(e.getContent());
        d.setDetail(toJsonOrNull(e.getDetail()));
        d.setOperatorId(e.getOperatorId());
        d.setOperatorName(e.getOperatorName());
        d.setSuccess(e.isSuccess() ? 1 : 0);
        d.setErrorMsg(e.getErrorMsg());
        d.setRequestId(e.getRequestId());
        d.setTraceId(e.getTraceId());
        d.setCreatedAt(LocalDateTime.ofInstant(e.getCreatedAt(), ZoneId.systemDefault()));
        mapper.insert(d);
    }

    private static String nvl(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }
}
