package cn.cheny.toolbox.property.token;

import java.util.List;

/**
 * 字符串规则提取
 *
 * @author by chenyi
 * @date 2021/2/8
 */
public class TokenParser {

    private final static TokenExtractor collectionExtractor = new TokenExtractor("[", "]");

    private final String fullToken;
    private final boolean isCollection;
    private String property;
    private String nextToken;
    private Integer[] collectionIndexes;

    public TokenParser(String token) {
        this.fullToken = token;
        int i = token.indexOf(".");
        if (i != -1) {
            this.property = token.substring(0, i);
            if (i == token.length() - 1) {
                throw new IllegalArgumentException("illegal token:" + token);
            }
            this.nextToken = token.substring(i + 1);
        } else {
            this.property = token;
        }
        List<String> collectionIdx = collectionExtractor.extract(property);
        boolean isCollection = collectionIdx.size() > 0;
        this.isCollection = isCollection;
        if (isCollection) {
            collectionIndexes = collectionIdx.stream().map(Integer::parseInt).toArray(Integer[]::new);
            property = property.substring(0, property.indexOf("["));
        }
    }

    public TokenParser next() {
        return nextToken != null ? new TokenParser(nextToken) : null;
    }

    public String getFullToken() {
        return fullToken;
    }

    public String getProperty() {
        return property;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public Integer[] getCollectionIndexes() {
        return collectionIndexes;
    }
}
