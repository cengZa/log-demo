package com.example.oplog.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.*;
/**
 * 在不修改方法签名的前提下，把“旧值/外部值/临时变量”放入模板可见的上下文
 */
public final class LogRecordContext {
    // 在线程池透传的ThreadLocal
    // 用于让 LogRecordContext 的上下文变量在异步/线程池场景下也能被模板解析到（如 {user{#id}} 里如果要访问上下文变量）
    private static final TransmittableThreadLocal<Deque<Map<String, Object>>> CTX = new TransmittableThreadLocal<>();

    private LogRecordContext() {
    }

    public static void push() {
        Deque<Map<String, Object>> dq = CTX.get();
        if (dq == null) {
            dq = new ArrayDeque<>();
            CTX.set(dq);
        }
        dq.push(new HashMap<>());
    }

    public static void put(String key, Object val) {
        Deque<Map<String, Object>> dq = CTX.get();
        if (dq == null || dq.isEmpty()) push();
        dq.peek().put(key, val);
    }

    public static Map<String, Object> variables() {
        Deque<Map<String, Object>> dq = CTX.get();
        if (dq == null || dq.isEmpty()) return Map.of();
        return dq.peek();
    }

    public static void pop() {
        Deque<Map<String, Object>> dq = CTX.get();
        if (dq != null && !dq.isEmpty()) dq.pop();
        if (dq != null && dq.isEmpty()) CTX.remove();
    }
}
