package cn.cheny.toolbox.property;

/**
 * 属性名工具类
 *
 * @author cheney
 * @date 2019-07-09
 */
public class PropertyNameUtils {

    private final static char UNDERLINE = "_".toCharArray()[0];

    private PropertyNameUtils() {
    }

    /**
     * 驼峰转下划线
     */
    public static String underline(String name) {
        char[] chars = name.toCharArray();
        int len = chars.length + 3;
        char[] newChars = new char[len];
        int j = 0;
        for (int i = 0; i < chars.length; i++, j++) {
            char c = chars[i];
            if (c >= 65 && c <= 90) {
                if (j == len) {
                    newChars = resize(newChars);
                    len = newChars.length;
                }
                newChars[j++] = UNDERLINE;
                c = (char) (c + 32);
            }
            newChars[j] = c;
        }
        return new String(newChars, 0, j);
    }

    /**
     * 下划线转驼峰
     */
    public static String hump(String name) {
        char[] chars = name.toCharArray();
        int len = chars.length;
        char[] newChars = new char[len];
        int index = 0;
        for (int i = 0; i < len; i++) {
            char c = chars[i];
            if (UNDERLINE == c) {
                char next = chars[++i];
                if (next >= 97 && next <= 122) {
                    next -= 32;
                }
                c = next;
            }
            newChars[index++] = c;
        }
        return new String(newChars, 0, index);
    }

    private static char[] resize(char[] chars) {
        char[] newChars = new char[chars.length + 3];
        System.arraycopy(chars, 0, newChars, 0, chars.length);
        return newChars;
    }

}
