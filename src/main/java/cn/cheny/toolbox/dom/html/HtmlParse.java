package cn.cheny.toolbox.dom.html;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * html解析提取，替换原文本
 *
 * @date 2021/3/25
 * @author by chenyi
 */
public class HtmlParse {

    private String html;

    private Document document;

    private List<NodeAndText> nodeAndTexts;

    private int textSize;

    public HtmlParse(String html) {
        html = StringEscapeUtils.unescapeHtml4(html);
        this.html = html;
        this.nodeAndTexts = new ArrayList<>();
        parse();
    }

    @SneakyThrows
    private void parse() {
        MyNodeVisitor myNodeVisitor = new MyNodeVisitor(this.nodeAndTexts);
        Document document = this.document = Jsoup.parse(html);
        document.traverse(myNodeVisitor);
        textSize = myNodeVisitor.getByteSize();
    }

    public String getHtml() {
        return html;
    }

    public int getTextSize() {
        return textSize;
    }

    public List<NodeAndText> getNodeAndTexts() {
        return nodeAndTexts;
    }

    public String replace() {
        for (NodeAndText nodeAndText : this.nodeAndTexts) {
            String result = nodeAndText.getResult();
            if (result == null) {
                throw new ToolboxRuntimeException("parse html error,not result");
            }
            nodeAndText.getNode().text(result);
        }
        return this.document.outerHtml();
    }

    private static class MyNodeVisitor implements NodeVisitor {

        private final List<NodeAndText> nodeAndTexts;

        private int byteSize;

        public MyNodeVisitor(List<NodeAndText> nodeAndTexts) {
            this.nodeAndTexts = nodeAndTexts;
            this.byteSize = 0;
        }

        @Override
        public void head(Node node, int depth) {
            if (node.nodeName().equals("#text")) {
                TextNode textNode = (TextNode) node;
                String text = textNode.text();
                if (StringUtils.isNotBlank(text) && !"\u00A0".equals(text)) {
                    byteSize += text.getBytes(StandardCharsets.UTF_8).length;
                    this.nodeAndTexts.add(new NodeAndText(textNode));
                }
            }
        }

        @Override
        public void tail(Node node, int depth) {
        }

        public int getByteSize() {
            return byteSize;
        }
    }

    @Data
    public static class NodeAndText {
        private TextNode node;
        private String text;
        private String result;

        public NodeAndText(TextNode node) {
            this.node = node;
            this.text = node.text();
        }
    }

}
