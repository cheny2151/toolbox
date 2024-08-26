package cn.cheny.toolbox.property.token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by chenyi
 * @date 2021/2/8
 */
public class TokenExtractor {

    private final char[] start;
    private final char[] end;

    public TokenExtractor(String start, String end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException();
        }
        this.start = start.toCharArray();
        this.end = end.toCharArray();
    }

    public List<String> extract(String text) {
        ArrayList<String> results = new ArrayList<>();
        char[] chars = text.toCharArray();
        int startTokenLen = start.length;
        int endTokenLen = end.length;
        int startLabel = -1;
        out:
        for (int cursor = 0; cursor < text.length(); ) {
            // start token
            if (chars[cursor] == start[0]) {
                for (int i = 0; i < startTokenLen && cursor + i < text.length(); ) {
                    if (start[i] != chars[cursor + i]) {
                        break;
                    }
                    i++;
                    if (i == startTokenLen) {
                        startLabel = cursor + startTokenLen;
                        cursor++;
                        continue out;
                    }
                }
            }
            // end token
            if (chars[cursor] == end[0]) {
                for (int i = 0; i < endTokenLen && cursor + i < text.length(); ) {
                    if (end[i] != chars[cursor + i]) {
                        break;
                    }
                    i++;
                    if (i == endTokenLen && startLabel != -1) {
                        results.add(text.substring(startLabel, cursor));
                        startLabel = -1;
                        cursor += i;
                        continue out;
                    }
                }
            }
            cursor++;
        }
        return results;
    }

}
