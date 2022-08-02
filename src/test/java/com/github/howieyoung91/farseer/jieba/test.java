package com.github.howieyoung91.farseer.jieba;

import com.github.howieyoung91.farseer.util.keyword.Keyword;
import com.github.howieyoung91.farseer.util.keyword.TFIDFAnalyzer;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;


@Slf4j
public class test {
    private JiebaSegmenter segmenter = new JiebaSegmenter();

    String[] sentences = new String[]{"美沃可视数码裂隙灯,检查眼前节健康状况", "欧美夏季ebay连衣裙 气质圆领通勤绑带收腰连衣裙 zc3730"};

    @Test
    public void segTest1() {
        for (String sentence : sentences) {
            List<SegToken> tokens = segmenter.process(sentence, JiebaSegmenter.SegMode.SEARCH);
            System.out.printf(Locale.getDefault(), "\n%s\n%s", sentence, tokens.toString());
            System.out.println(tokens);
            TFIDFAnalyzer analyzer = new TFIDFAnalyzer();
            List<Keyword> keywords = analyzer.analyze(sentence, 10);
            System.out.println(keywords);
        }
    }

    @Test
    public void tfidfTest1() {
        String        content  = " List<RecordSeg> recordSegs = recordSegDao.selectRecordBySeg(segmentation.getId());";
        TFIDFAnalyzer analyzer = new TFIDFAnalyzer();
        List<Keyword> keywords = analyzer.analyze(content, 10);
        System.out.println(keywords.toString());
    }
}