package com.example.demo.infrastructure;

import com.example.oplog.operator.Operator;
import com.example.oplog.operator.OperatorGetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 获取“当前操作者”的实现
 */
@Component
public class DemoOperatorGetService implements OperatorGetService {
    @Override
    public Operator getCurrent() {
        var at = RequestContextHolder.getRequestAttributes();
        if (at instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();
            String id = req.getHeader("X-User-Id");
            String name = req.getHeader("X-User-Name");
            if (id != null && !id.isBlank()) {
                return new Operator(id, (name == null || name.isBlank()) ? id : name);
            }
        }
        return new Operator("anonymous", "anonymous");
    }
}
