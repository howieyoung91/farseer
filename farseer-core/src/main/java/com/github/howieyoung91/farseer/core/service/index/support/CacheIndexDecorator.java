/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.service.index.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.howieyoung91.farseer.core.config.CacheKeys;
import com.github.howieyoung91.farseer.core.entity.Document;
import com.github.howieyoung91.farseer.core.entity.Index;
import com.github.howieyoung91.farseer.core.pojo.DocumentDto;
import com.github.howieyoung91.farseer.core.util.Redis;

import java.util.Collection;
import java.util.List;

/**
 * 实现缓存
 *
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/11/19 16:53]
 */
public class CacheIndexDecorator extends SegmentCapableIndexer {
    private final SegmentCapableIndexer indexer;
    private final Redis                 redis;

    private CacheIndexDecorator(SegmentCapableIndexer indexer, Redis redis) {
        this.indexer = indexer;
        this.redis = redis;
    }

    public static CacheIndexDecorator decorate(SegmentCapableIndexer indexer, Redis redis) {
        return new CacheIndexDecorator(indexer, redis);
    }

    @Override
    public Collection<Index> getIndices(String documentId, Page<Index> page) {
        Collection<Index> indices = indexer.getIndices(documentId, page);
        cacheIndices(documentId, indices);
        return indices;
    }

    @Override
    public int deleteIndices(String documentId) {
        int row = indexer.deleteIndices(documentId);
        if (row != 0) {
            redis.del(CacheKeys.indicesOfDocumentKey(documentId));
        }
        return row;
    }

    @Override
    public Collection<Index> index(List<Document> documents) {
        return indexer.index(documents);
    }

    @Override
    public Collection<DocumentDto> doSearchByWord(List<String> words, Page<Index> page) {
        return indexer.doSearchByWord(words, page);
    }

    @Override
    public Collection<DocumentDto> doSearchBySentence(List<String> words, Page<Index> page) {
        return indexer.doSearchBySentence(words, page);
    }

    @Override
    public Collection<DocumentDto> doSearchByQueryString(List<String> words, Page<Index> page) {
        return indexer.doSearchByQueryString(words, page);
    }

    private void cacheIndices(String documentId, Collection<Index> indices) {
        if (indices.isEmpty()) {
            redis.cfadd(CacheKeys.indicesDocumentIdCuckooFilter(), documentId);
        }
        else {
            redis.kvSet(CacheKeys.indicesOfDocumentKey(documentId), indices, 1000 * 60 * 30);
        }
    }
}
