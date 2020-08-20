package cn.cheny.toolbox.expression.demo;

import cn.cheny.toolbox.expression.executor.ExpressionExecutor;
import cn.cheny.toolbox.expression.parse.AviatorExpressionParser;
import cn.cheny.toolbox.expression.parse.ExpressionParser;
import cn.cheny.toolbox.expression.parse.ReflectExpressionParser;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cheney
 * @date 2019-12-07
 */
public class Main {

    @Test
    public void test0() {
        long l = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            test();
            test2();
            test3();
            test4();
            test5();
            test6();
            test7();
            test8();
            test9();
            test10();
            test11();
        }
        System.out.println((System.currentTimeMillis() - l) / 1000);
    }

    @Test
    public void test() {
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("print(toJson(date_format(date,'yyyy-MM-dd')))");
        HashMap<String, Object> env = new HashMap<>();
        env.put("date", new Date());
        expressionExecutor.execute(env);
    }


    @Test
    public void test2() {
        Map<String, Object> args = new HashMap<>();
        args.put("a", BigDecimal.valueOf(1));
        args.put("b", BigDecimal.valueOf(2));
        args.put("c", "A");
        args.put("a1", BigDecimal.valueOf(2));
        args.put("b1", BigDecimal.valueOf(1));
        args.put("c1", "B");
        AviatorExpressionParser aviatorExpressionParser = AviatorExpressionParser.getInstance();
        ExpressionExecutor executor = aviatorExpressionParser.parseExpression("println(ifs(a>b-a,c,a1>b1,c1))");
        Object execute = executor.execute(args);
        System.out.println(execute);
    }

    @Test
    public void test3() {
        Map<String, Object> env = new HashMap<>();
        env.put("a", BigDecimal.valueOf(1));
        env.put("b", BigDecimal.valueOf(2));
        env.put("c", 10);
        env.put("a1", BigDecimal.valueOf(2));
        env.put("b1", BigDecimal.valueOf(1));
        env.put("c1", 100);
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("ifs(!(a>b-a),(c1-c),(a1>b1),c-c1)");
        System.out.println(expressionExecutor.execute(env));
    }

    @Test
    public void test4() {
        Map<String, Object> env = new HashMap<>();
        env.put("a", BigDecimal.valueOf(1));
        env.put("b", BigDecimal.valueOf(2));
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("a>b");
        System.out.println(expressionExecutor.execute(env));
    }

    @Test
    public void test5() {
        Map<String, Object> env = new HashMap<>();
        env.put("a", BigDecimal.valueOf(1));
        env.put("b", BigDecimal.valueOf(2));
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("println(a+b)");
        expressionExecutor.execute(env);
    }

    @Test
    public void test6() {
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("print(toJson(a)+toJson(2))");
        HashMap<String, Object> env = new HashMap<>();
        env.put("a", 1);
        expressionExecutor.execute(env);
    }

    @Test
    public void test7() {
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("print(contains(a,'x') && contains(b,'b') || ! false &&contains(b,'b') )");
        HashMap<String, Object> env = new HashMap<>();
        env.put("a", "testa");
        env.put("b", "testb");
        expressionExecutor.execute(env);
    }

    @Test
    public void test8() {
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("print(abs(-a)-1+(2-1)+abs(-5)+ (-a))");
        HashMap<String, Object> env = new HashMap<>();
        env.put("a", 1);
        expressionExecutor.execute(env);
    }

    @Test
    public void test9() {
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor =
                expressionParser.parseExpression("(业务类型=='在线支付'||业务类型=='交易付款')||(业务类型=='转账'&&contains(备注,'基金代发任务'))||(业务类型=='交易分账'&&contains(备注,'境内商户结算'))");
        HashMap<String, Object> env = new HashMap<>();
        env.put("业务类型", "交易分账");
        env.put("备注", "境内商户结算");
        System.out.println(expressionExecutor.execute(env));

        ExpressionExecutor expressionExecutor2 =
                expressionParser.parseExpression("(业务类型=='在线支付'||业务类型=='交易付款')||(业务类型=='转账'&&contains(备注,'基金代发任务'))||(业务类型=='交易分账'&&contains(备注,'境内商户结算'))");
        HashMap<String, Object> env2 = new HashMap<>();
        env2.put("业务类型", "其他分账");
        env2.put("备注", "其他结算");
        System.out.println(expressionExecutor2.execute(env2));
    }

    @Test
    public void test10() {
        ReflectExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("replace('10.00元','元','')");
        System.out.println(expressionExecutor.execute(null));
    }

    @Test
    public void test11() {
        ReflectExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor = expressionParser.parseExpression("nil");
        System.out.println(expressionExecutor.execute(null));
    }

    @Test
    public void test12() {
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor2 =
                expressionParser.parseExpression("ifs(业务类型=='其它'&&contains(备注,'天猫物流破损险'),substring(备注,21,18),业务类型=='转账'&&contains(备注,'基金代发任务'),substring(备注,5,18),业务类型=='其它'&&(contains(备注,'售后支付')||contains(备注,'商家保证金理赔')||contains(备注,'保证金退款'))||(业务类型=='转账'&&(contains(备注,'天天特卖')||contains(备注,'售后退款'))),substring(商户订单号,5,18),true,业务基础订单号)");
        HashMap<String, Object> env2 = new HashMap<>();
        env2.put("业务类型", "其他分账");
        env2.put("备注", "其他结算其他结算其他结算其他结算其他结算其他结算其他结算其他结算其他结算其他结算其他结算其他结算其他结算");
        env2.put("商户订单号", "商户订单号商户订单号商户订单号商户订单号商户订单号商户订单号商户订单号商户订单号商户订单号商户订单号");
        System.out.println(expressionExecutor2.execute(env2));
    }

    @Test
    public void test13() {
        ExpressionParser expressionParser = ReflectExpressionParser.getInstance();
        ExpressionExecutor expressionExecutor2 =
                expressionParser.parseExpression("to_number(服务费)+to_number(团长佣金)");
        HashMap<String, Object> env2 = new HashMap<>();
        env2.put("服务费", "1");
        env2.put("团长佣金", "2");
        System.out.println(expressionExecutor2.execute(env2));
    }
}
