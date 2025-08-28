package com.example.oplog.aop;

import com.example.oplog.annotation.LogRecord;
import com.example.oplog.context.LogRecordContext;
import com.example.oplog.func.FunctionRenderer;
import com.example.oplog.func.ParseFunctionRegistry;
import com.example.oplog.operator.Operator;
import com.example.oplog.operator.OperatorGetService;
import com.example.oplog.persist.LogEntry;
import com.example.oplog.persist.LogRecordService;
import com.example.oplog.spel.SpelEvaluator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * 日志记录切面
 */

@Aspect
public class LogRecordAspect {
    private final SpelEvaluator spel;
    private final ParseFunctionRegistry reg;
    private final LogRecordService persist;
    private final OperatorGetService operatorGet;

    public LogRecordAspect(SpelEvaluator spel, ParseFunctionRegistry reg,
                           LogRecordService persist, OperatorGetService operatorGet) {
        this.spel = spel;
        this.reg = reg;
        this.persist = persist;
        this.operatorGet = operatorGet;
    }

    // 拦截带有 @LogRecord 注解的方法
    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint pjp, LogRecord log) throws Throwable {
        // 反射拿到方法对象
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // 取被代理对象
        Object target = pjp.getTarget();
        // 取方法实参
        Object[] args = pjp.getArgs();

        // 入栈上下文
        LogRecordContext.push();
        Object ret = null;
        Throwable err = null;
        try {
            // 执行业务方法，捕获异常，抛回业务调用方
            ret = pjp.proceed();
            return ret;
        } catch (Throwable t) {
            err = t;
            throw t;
        } finally {
            try {
                // 构造SpEL上下文
                // EvaluationContext 负责给表达式提供数据和解析规则
                EvaluationContext ctx = spel.createContext(target, method, args, ret, err);
                boolean cond = spel.bool(log.condition(), ctx, true);

                // 若解析失败则跳过
                if (!cond) return null;

                // 解析操作者信息，订单信息，种类信息，内容信息
                Operator op = resolveOperator(log.operator(), ctx);
                String bizNo = spel.str(log.bizNo(), ctx);
                String category = spel.str(log.category(), ctx);
                String detail = FunctionRenderer.render(spel.str(log.detail(), ctx), ctx, reg);

                // 选择成功 / 失败模版
                String tpl = (err == null) ? log.success() :
                        (log.fail() == null || log.fail().isBlank()) ? log.success() : log.fail();
                // 先用SpEL解析 #xxx, 再用FunctionRenderer处理自定义函数片段
                String content = FunctionRenderer.render(spel.str(tpl, ctx), ctx, reg);

                // 构造日志条目
                LogEntry e = new LogEntry();
                e.setTenant("default");
                e.setCategory(category);
                e.setBizNo(bizNo);
                e.setSuccess(err == null);
                e.setContent(content);
                e.setDetail(detail);
                e.setOperatorId(op.id());
                e.setOperatorName(op.name());
                e.setRequestId(null);
                e.setTraceId(null);
                e.setErrorMsg(err == null ? null : err.getMessage());
                e.setCreatedAt(Instant.now());

                if (TransactionSynchronizationManager.isActualTransactionActive()) {
                    // 如果当前存在事务，注册一个同步器，在事务提交后记录日志
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            persist.record(e);
                        }
                    });
                } else {
                    // 否则直接记录日志
                    persist.record(e);
                }
            } finally {
                // 出栈上下文
                LogRecordContext.pop();
            }
        }
    }

    private Operator resolveOperator(String expr, EvaluationContext ctx) {
        String idOrName = (expr == null || expr.isBlank()) ? null : spel.str(expr, ctx);
        if (idOrName == null || idOrName.isBlank()) return operatorGet.getCurrent();
        return new Operator(idOrName, idOrName);
    }
}
