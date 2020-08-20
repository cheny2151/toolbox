package cn.cheny.toolbox.expression.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表达式测试结果
 * 1.7新增
 */
@Data
@AllArgsConstructor
public class TestResult {

    private String errorMsg;
    private boolean passed;

    public static TestResult success() {
        return new TestResult("success", true);
    }

    public static TestResult fail(String expression, Exception e) {
        return new TestResult("表达式 " + expression + " 测试不通过:" + e.getMessage(), false);
    }

}
