package cn.cheny.toolbox.property;

import cn.cheny.toolbox.property.token.TokenExtractor;
import org.junit.Test;

import java.util.List;

/**
 * @author by chenyi
 * @date 2024/8/26
 */
public class TestToken {

    @Test
    public void test1() {
        TokenExtractor tokenExtractor = new TokenExtractor("#{", "}");
        List<String> extract = tokenExtractor.extract("select * from student where code not like \"#{\" and name = #{username}");
        System.out.println(extract);
    }

    @Test
    public void test2() {
        TokenExtractor tokenExtractor = new TokenExtractor("((", "))");
        List<String> extract = tokenExtractor.extract("select id,merchant_id,name,price from product where price in (((prices))) and code != \"((\" and merchant_id in(((merchant_ids))) and code != \"))\" and status = ((status))");
        System.out.println(extract);
    }

}
