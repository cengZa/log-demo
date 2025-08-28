package com.example.oplog.func;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析并渲染模板内的 自定义函数段 {func{SpEL}}。
 */
public class FunctionRenderer {
    private static final Pattern P = Pattern.compile("\\{([a-zA-Z0-9_]+)\\{([^{}]+)\\}\\}");
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private static final Map<String, Expression> CACHE = new ConcurrentHashMap<>();

    public static String render(String tpl, EvaluationContext ctx, ParseFunctionRegistry reg) {
        if (tpl == null || tpl.isBlank()) return "";
        StringBuffer sb = new StringBuffer();
        Matcher m = P.matcher(tpl);
        while (m.find()) {
            String func = m.group(1);
            String spelExpr = m.group(2);
            String val = CACHE.computeIfAbsent(spelExpr, PARSER::parseExpression).getValue(ctx, String.class);
            var f = reg.get(func);
            String replaced = f == null ? val : f.apply(val);
            m.appendReplacement(sb, Matcher.quoteReplacement(replaced));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
