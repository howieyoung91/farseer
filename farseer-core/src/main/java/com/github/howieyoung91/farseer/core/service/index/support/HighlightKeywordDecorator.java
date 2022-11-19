/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.service.index.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.howieyoung91.farseer.core.entity.Document;
import com.github.howieyoung91.farseer.core.entity.Index;
import com.github.howieyoung91.farseer.core.pojo.DocumentDto;
import com.github.howieyoung91.farseer.core.word.support.AcHighlighter;

import java.util.Collection;
import java.util.List;

/**
 * 高亮功能装饰器
 *
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/11/18 17:13]
 */
public class HighlightKeywordDecorator extends SegmentCapableIndexer {
    private final SegmentCapableIndexer indexer;

    private HighlightKeywordDecorator(SegmentCapableIndexer indexer) {
        this.indexer = indexer;
    }

    public static SegmentCapableIndexer decorate(SegmentCapableIndexer indexer) {
        return new HighlightKeywordDecorator(indexer);
    }

    @Override
    protected Collection<DocumentDto> doSearchByWord(List<String> words, Page<Index> page) {
        Collection<DocumentDto> documentDtos = indexer.doSearchByWord(words, page);
        highlight(documentDtos, words.toArray(new String[]{}));
        return documentDtos;
    }

    @Override
    protected Collection<DocumentDto> doSearchBySentence(List<String> words, Page<Index> page) {
        Collection<DocumentDto> documentDtos = indexer.doSearchBySentence(words, page);
        highlight(documentDtos, words.toArray(new String[]{}));
        return documentDtos;
    }

    @Override
    protected Collection<DocumentDto> doSearchByQueryString(List<String> words, Page<Index> page) {
        Collection<DocumentDto> documentDtos = indexer.doSearchByQueryString(words, page);
        highlight(documentDtos, words.toArray(new String[]{}));
        return documentDtos;
    }

    private static void highlight(Collection<DocumentDto> documents, String... words) {
        for (DocumentDto documentDto : documents) {
            AcHighlighter highlighter = new AcHighlighter(
                    documentDto.getHighlightPrefix(), documentDto.getHighlightSuffix(), words);
            String highlightedText = highlighter.highlight(documentDto.getText());
            documentDto.setText(highlightedText);
        }
    }


    @Override
    public Collection<Index> getIndices(String documentId, Page<Index> page) {
        return indexer.getIndices(documentId, page);
    }

    @Override
    public int deleteIndices(String documentId) {
        return indexer.deleteIndices(documentId);
    }

    @Override
    public Collection<Index> index(List<Document> documents) {
        return indexer.index(documents);
    }
}
