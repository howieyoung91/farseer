/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.service.index.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.howieyoung91.farseer.core.entity.Document;
import com.github.howieyoung91.farseer.core.entity.Index;
import com.github.howieyoung91.farseer.core.pojo.DocumentDto;
import com.github.howieyoung91.farseer.core.word.SensitiveFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/11/18 18:44]
 */
public class SensitiveFilterDecorator extends SegmentCapableIndexer {
    private final SegmentCapableIndexer indexer;
    private final SensitiveFilter       filter;

    private SensitiveFilterDecorator(SegmentCapableIndexer indexer, SensitiveFilter filter) {
        this.indexer = indexer;
        this.filter = filter;
    }

    public static SegmentCapableIndexer decorate(SegmentCapableIndexer indexer, SensitiveFilter filter) {
        return new SensitiveFilterDecorator(indexer, filter);
    }

    @Override
    public List<DocumentDto> doSearchByWord(List<String> words, Page<Index> page) {
        if (isSensitiveWord(words.get(0))) {
            return new ArrayList<>();
        }
        List<DocumentDto> documentDtos = indexer.doSearchByWord(words, page);
        filterSensitivesInDocument(documentDtos);
        return documentDtos;
    }

    @Override
    public Collection<DocumentDto> doSearchBySentence(List<String> words, Page<Index> page) {
        filterSensitivesInWords(words);
        Collection<DocumentDto> documentDtos = indexer.doSearchBySentence(words, page);
        filterSensitivesInDocument(documentDtos);
        return documentDtos;
    }

    @Override
    public List<DocumentDto> doSearchByQueryString(List<String> words, Page<Index> page) {
        filterSensitivesInWords(words);
        List<DocumentDto> documentDtos = indexer.doSearchByQueryString(words, page);
        filterSensitivesInDocument(documentDtos);
        return documentDtos;
    }

    private void filterSensitivesInWords(List<String> words) {
        words.removeIf(this::isSensitiveWord);
    }

    private void filterSensitivesInDocument(Collection<DocumentDto> documentDtos) {
        for (DocumentDto documentDto : documentDtos) {
            String filteredText = filter.filter(documentDto.getText());
            documentDto.setText(filteredText);
        }
    }

    private boolean isSensitiveWord(String word) {
        return filter.isSensitive(word);
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
