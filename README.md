# Operation Log Starter（AOP + SpEL 的可读操作日志）

> 面向业务“操作日志（Operation Log）”的轻量级 Starter。
> 目标：可读（面向用户/客服）、解耦（与业务零侵入）、可扩展（函数/持久化/操作人策略可插拔）、一致性（事务提交后再落库）。

## 设计与原理
```less
@LogRecord(success="...", fail="...", bizNo="#{#req.orderNo}", detail="...", condition="...")
      │
      ▼  AOP 切面拦截 (@Around)
[LogRecordAspect]
  ├─ 前置：LogRecordContext.push()（为本次调用压栈一份变量Map）
  ├─ 调用业务方法：pjp.proceed()
  ├─ 后置：
  │    ├─ 构建 EvaluationContext（含：方法参数/返回值/_errorMsg/上下文变量）
  │    ├─ 解析 SpEL 模板（TemplateParserContext）与 {func{#expr}} 自定义函数
  │    └─ 组装 LogEntry（tenant/category/bizNo/content/detail/operator/…）
  ├─ 事务存在 → registerSynchronization(afterCommit) 落库
  └─ finally：LogRecordContext.pop()（出栈，避免变量泄漏/覆盖）
```

## 依赖安装
```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>operation-log-starter</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 快速开始

### 在业务方法上打注解
```java
@Transactional
@LogRecord(
  success = "修改了订单配送地址：从“#{#oldAddress}”到“#{#req.address}”",
  fail    = "尝试修改订单(#{#req.orderNo})配送地址失败：原因=#{_errorMsg}",
  bizNo   = "#{#req.orderNo}",
  category= "DELIVERY",
  condition = "!#req.address.equals(#oldAddress)",
  detail = "联系人手机号：{mask{#req.phone}}"
)
public void changeAddress(ChangeAddressReq req) {
    // 读取旧值并放入上下文（对日志可见，不污染方法签名）
    var old = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>()
                  .eq(OrderDO::getOrderNo, req.orderNo()));
    String oldAddr = old == null ? "(空)" : (old.getAddress() == null ? "(空)" : old.getAddress());
    LogRecordContext.put("oldAddress", oldAddr);

    // 执行业务更新（事务内）
    int updated = orderMapper.update(null, new LambdaUpdateWrapper<OrderDO>()
        .set(OrderDO::getAddress, req.address())
        .eq(OrderDO::getOrderNo, req.orderNo()));
    if (updated == 0) throw new IllegalStateException("订单不存在或未更新");
}

```

### 持久化日志
（默认写“业务日志”文件；这里示例 MyBatis-Plus → MySQL）

```java
@Component
public class MyBatisPlusLogRecordService implements LogRecordService {
    @Override public void record(LogEntry e) {
        OpLogDO d = new OpLogDO();
        d.setTenant(nvl(e.getTenant(), "default"));
        // ...填充各字段
        d.setDetail(toJsonOrNull(e.getDetail()));
        mapper.insert(d);
    }
}
```

### 自定义函数

```java
public class MaskFunction implements ParseFunction {
    @Override
    public String name() {
        return "mask";
    }

    @Override
    public String apply(String value) {
        if (value == null || value.isBlank()) return "";
        String v = value.replaceAll(" +", "");
        if (v.length() >= 7) return v.substring(0, 3) + "****" + v.substring(v.length() - 4);
        return "***";
    }
}
```

> **FunctionRenderer 在整条流水线的作用**
> 整体渲染流程是“两段式”的：
> 1. **SpEL 模板阶段（SpelEvaluator#str）**
>   - 识别并求值 #{...} 片段（混排模板），拿到纯文本字符串。
>   - 这一步只处理 SpEL，不处理 {func{...}}。
> 2. **自定义函数阶段（FunctionRenderer#render）**
>   - 在上一步的纯文本里，扫描形如 {funcName{#spel}} 的片段；
>   - 先用同一个 EvaluationContext 求 #spel 的值；
>   - 再把值交给 funcName 对应的 ParseFunction.apply(...) 做“文案增强/脱敏/查显示名”等；
>   - 把整个 {func{...}} 替换为结果。

## 流程
1. Controller 接受请求，X-User-Id 传来 u001；服务端通过 OperatorGetService 查到“小明”
2. 切面拦截 changeAddress，push() 新的上下文栈帧；
3. 业务查询旧地址→放入 LogRecordContext（oldAddress）；
4. 执行业务更新；
5. 返回后，切面构建 EvaluationContext（含 #req/#_ret/#_errorMsg 与 #oldAddress）；
6. 解析模板 → "修改了订单配送地址：从“金灿灿小区”到“银盏盏小区”"；
7. 事务提交后 afterCommit() 落库 op_log；
8. 最终在 DB/检索系统看到可读文案，按 bizNo=NO1001 可串联订单全链路。

## 踩坑记录与解决方案

### Unsupported character '：'
- 原因：SpEL把中文文案当表达式解析导致：
- 修复：采用模板模式（"xxx #{#var} yyy"），解析端检测 #{ 用 TemplateParserContext 解析。

### MySQL JSON 列报 “The document is empty.”
- 原因：插入空字符串不是合法 JSON；
- 修复：空白→NULL；非 JSON 文本→自动转合法 JSON 字符串。

### operator_name 变 ??
- 原因：HTTP Header规范只允许ASCII 而非中文！
- 修复：Header 只传 ID，姓名服务端查询；或客户端对 Header URL 编码后服务端解码；或改为放在 Body/JWT。
本文采用传英ASCII字符hh

## 笔者一些思考：



**参考：**
如何优雅地记录操作日志？
https://tech.meituan.com/2021/09/16/operational-logbook.html