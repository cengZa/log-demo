package com.example.oplog.spel;

import com.example.oplog.context.LogRecordContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一的SpEL解析器，缓存Expression，提高性能
 * 把字符串模板里的 #req.address、_errorMsg 等表达式解析出字符串/布尔值
 */

@Slf4j
public class SpelEvaluator {
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final Map<String, Expression> cache = new ConcurrentHashMap<>();

    public EvaluationContext createContext(Object target, Method method, Object[] args, Object ret, Throwable err) {
        var pnd = new DefaultParameterNameDiscoverer();
        MethodBasedEvaluationContext ctx = new MethodBasedEvaluationContext(target, method, args, pnd);
        LogRecordContext.variables().forEach(ctx::setVariable);
        ctx.setVariable("_ret", ret);
        ctx.setVariable("_errorMsg", err == null ? null : err.getMessage());

        // ====== 调试快照输出（新增）======
        String[] paramNames = pnd.getParameterNames(method);
        Map<String,Object> snapshot = new LinkedHashMap<>();
        snapshot.put("_root", target.getClass().getName());
        // 方法参数
        Map<String,Object> params = new LinkedHashMap<>();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                params.put(paramNames[i], i < args.length ? args[i] : null);
            }
        }
        snapshot.put("_params", params);
        // ThreadLocal 变量
        snapshot.put("_vars", new LinkedHashMap<>(LogRecordContext.variables()));
        // 特殊变量
        snapshot.put("_ret", ret);
        snapshot.put("_errorMsg", err == null ? null : err.getMessage());
        log.info("[SpEL ctx snapshot] method={} snapshot={}", method, snapshot);
        // ================================

        return ctx;
    }

    public String str(String tpl, EvaluationContext ctx) {
        if (tpl == null || tpl.isBlank()) return "";
        // 带 #{...} => 模板解析
        if (tpl.contains("#{")) {
            String key = "TEMPLATE::" + tpl; // 与纯表达式分开缓存
            return cache.computeIfAbsent(key, k -> parser.parseExpression(tpl, new TemplateParserContext()))
                    .getValue(ctx, String.class);
        }
        // 纯表达式（#xxx ）
        if (tpl.trim().startsWith("#") || tpl.trim().startsWith("T(")) {
            return cache.computeIfAbsent(tpl, parser::parseExpression).getValue(ctx, String.class);
        }
        // 纯文本
        return tpl;
    }

    public boolean bool(String expr, EvaluationContext ctx, boolean def) {
        if (expr == null || expr.isBlank()) return def;
        Boolean v = cache.computeIfAbsent(expr, parser::parseExpression).getValue(ctx, Boolean.class);
        return v != null ? v : def;
    }
}
