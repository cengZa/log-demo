package com.example.oplog.autoconfig;

import com.example.oplog.aop.LogRecordAspect;
import com.example.oplog.func.MaskFunction;
import com.example.oplog.func.ParseFunction;
import com.example.oplog.func.ParseFunctionRegistry;
import com.example.oplog.operator.OperatorGetService;
import com.example.oplog.persist.DefaultLogRecordService;
import com.example.oplog.persist.LogRecordService;
import com.example.oplog.spel.SpelEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class OperationLogAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SpelEvaluator spelEvaluator() {
        return new SpelEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean
    public ParseFunctionRegistry parseFunctionRegistry(List<ParseFunction> functions) {
        return new ParseFunctionRegistry(functions);
    }

    @Bean
    @ConditionalOnMissingBean
    public ParseFunction maskFunction() {
        return new MaskFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogRecordService logRecordService() {
        return new DefaultLogRecordService();
    }

    @Bean
    public LogRecordAspect logRecordAspect(SpelEvaluator spel, ParseFunctionRegistry reg,
                                           LogRecordService svc, OperatorGetService op) {
        return new LogRecordAspect(spel, reg, svc, op);
    }
}
