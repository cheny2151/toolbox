package cn.cheny.toolbox.property.token;

import java.util.ArrayList;
import java.util.List;

/**
 * @date 2021/2/8
 * @author by chenyi
 */
public class TokenExtractor {

    private char[] start;
    private char[] end;

    public TokenExtractor(String start, String end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException();
        }
        this.start = start.toCharArray();
        this.end = end.toCharArray();
    }

    public List<String> extract(String token) {
        ArrayList<String> results = new ArrayList<>();
        char[] chars = token.toCharArray();
        int idx = 0;
        int endLen = end.length;
        while ((idx = find(start, chars, idx)) != -1) {
            int subStart = idx + 1;
            if ((idx = find(end, chars, idx + 1)) != -1) {
                int subEnd = idx - endLen + 1;
                results.add(token.substring(subStart, subEnd));
            } else {
                break;
            }
        }
        return results;
    }

    /**
     * 从startIdx开始在beFind中查找target，查询到则返回target结尾字符位置，否则返回-1
     */
    private int find(char[] target, char[] beFind, int startIdx) {
        int targetLen = target.length;
        int idx = 0;
        char c = target[idx];
        for (int i = startIdx; i < beFind.length; i++) {
            if (c == beFind[i]) {
                if (++idx == targetLen) {
                    return i;
                }
                c = target[idx];
            } else if (idx != 0) {
                idx = 0;
                c = target[idx];
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        TokenExtractor tokenExtractor = new TokenExtractor("#{", "}");
        List<String> extract = tokenExtractor.extract("select * from t where id = #{id}");
        System.out.println(extract);
    }
}
