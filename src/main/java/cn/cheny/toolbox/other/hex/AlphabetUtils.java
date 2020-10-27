package cn.cheny.toolbox.other.hex;

/**
 * 字母工具类
 *
 * @author cheney
 * @date 2020-10-20
 */
public class AlphabetUtils {

    /**
     * 字母个数
     */
    private final static int ALPHABET_SIZE = 26;

    /**
     * A-Z/a-z进制转10进制
     *
     * @param alphabet 字母A-Z/a-z
     * @return 十进制
     */
    public static int toDec(String alphabet) {
        int dec = 0;
        alphabet = alphabet.toUpperCase();
        char[] chars = alphabet.toCharArray();
        int power = 0;
        for (int i = chars.length - 1; i >= 0; i--) {
            dec += (chars[i] - 64) * Math.pow(ALPHABET_SIZE, power++);
        }
        return dec;
    }

}
