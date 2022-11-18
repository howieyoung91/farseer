/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.service.index.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.howieyoung91.farseer.core.entity.Index;
import com.github.howieyoung91.farseer.core.entity.Token;
import com.github.howieyoung91.farseer.core.pojo.DocumentDto;
import com.github.howieyoung91.farseer.core.service.index.Indexer;
import com.github.howieyoung91.farseer.core.util.StringUtil;
import com.github.howieyoung91.farseer.core.word.Keyword;
import com.github.howieyoung91.farseer.core.word.support.TFIDFAnalyzer;
import com.huaban.analysis.jieba.JiebaSegmenter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

public abstract class SegmentCapableIndexer implements Indexer {
    @Resource
    private JiebaSegmenter segmenter;
    @Resource
    private TFIDFAnalyzer  analyzer;

    @Override
    public List<DocumentDto> searchByWord(String word, Page<Index> page) {
        ArrayList<String> words = new ArrayList<>(1);
        words.add(word);
        return doSearchByWord(words, page);
    }

    @Override
    public Collection<DocumentDto> searchBySentence(String sentence, Page<Index> page) {
        return doSearchBySentence(segmentOnSearchMode(sentence), page);
    }

    @Override
    public List<DocumentDto> searchByQueryString(String query, Page<Index> page) {
        List<String> words = Arrays.stream(StringUtil.splitByBlank(query)).collect(Collectors.toList());
        return doSearchByQueryString(words, page);
    }

    protected abstract List<DocumentDto> doSearchByWord(List<String> words, Page<Index> page);

    protected abstract Collection<DocumentDto> doSearchBySentence(List<String> words, Page<Index> page);

    protected abstract List<DocumentDto> doSearchByQueryString(List<String> words, Page<Index> page);

    protected List<String> segmentOnSearchMode(String text) {
        return segment(text, JiebaSegmenter.SegMode.SEARCH);
    }

    /**
     * 对一段文本进行分词
     */
    protected List<String> segmentOnIndexMode(String text) {
        return segment(text, JiebaSegmenter.SegMode.INDEX);
    }

    private List<String> segment(String text, JiebaSegmenter.SegMode mode) {
        return segmenter.process(text, mode).stream()
                .filter(segToken -> StringUtils.isNotBlank(segToken.word)) // 去除空白文本
                .map(segToken -> segToken.word)
                .collect(Collectors.toList());
    }

    /**
     * 分析一段文本中的关键词
     */
    protected Map<String, Keyword> analyze(String text, int number) {
        List<Keyword> keywords = analyzer.analyze(text, number);
        return keywords.stream().collect(Collectors.toMap(keyword -> keyword.getName().toLowerCase(Locale.ENGLISH), keyword -> keyword, (a, b) -> b));
    }

    /**
     * 计算某个 token 的 score
     */
    protected static double calcScore(Map<String, Keyword> keywords, Token token) {
        Keyword keyword = keywords.get(token.getWord());
        return keyword == null ? 0 : keyword.getTFIDF();
    }
}
