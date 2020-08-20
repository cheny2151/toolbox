package cn.cheny.toolbox.expression.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 参数段类实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Arg {

    public final static List<Arg> EMPTY_ARG = new ArrayList<>(0);
    // 值
    private Object value;
    // 类型：0:常量,1:函数,2:运算,3:组合段落
    private short type;
    // 类型枚举值
    public final static short CONSTANT = 0;
    public final static short FUNC = 1;
    public final static short ORIGIN = 2;
    public final static short COMBINATION = 3;

}
