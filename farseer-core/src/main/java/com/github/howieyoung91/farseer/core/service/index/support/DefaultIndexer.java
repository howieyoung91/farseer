/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.service.index.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.howieyoung91.farseer.core.annotation.Log;
import com.github.howieyoung91.farseer.core.annotation.NoConvertedDocumentsConverter;
import com.github.howieyoung91.farseer.core.entity.Document;
import com.github.howieyoung91.farseer.core.entity.Index;
import com.github.howieyoung91.farseer.core.entity.Token;
import com.github.howieyoung91.farseer.core.mapper.IndexMapper;
import com.github.howieyoung91.farseer.core.pojo.DocumentDto;
import com.github.howieyoung91.farseer.core.pojo.IndexInfo;
import com.github.howieyoung91.farseer.core.service.DocumentService;
import com.github.howieyoung91.farseer.core.service.TokenService;
import com.github.howieyoung91.farseer.core.util.Factory;
import com.github.howieyoung91.farseer.core.word.Keyword;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultIndexer extends SegmentCapableIndexer {
    @Resource
    private IndexMapper     indexMapper;
    @Resource
    private DocumentService documentService;
    @Resource
    private TokenService    tokenService;

    @Override
    public List<Index> getIndices(String documentId, Page<Index> page) {
        return indexMapper.selectPage(Factory.resolvePage(page),
                        Factory.createLambdaQueryWrapper(Index.class)
                                .eq(Index::getDocumentId, documentId).orderByDesc(Index::getScore))
                .getRecords();
    }

    @Override
    @Transactional
    @Log.Delete(operator = "delete document: ")
    public int deleteIndices(String documentId) {
        documentService.deleteById(documentId);
        return indexMapper.delete(Factory.createLambdaQueryWrapper(Index.class)
                .eq(Index::getDocumentId, documentId));
    }

    @Override
    @Transactional
    @Log.IndexedDocument(operator = "index documents: ", convert = NoConvertedDocumentsConverter.class)
    public Collection<Index> index(List<Document> documents) {
        documentService.insert(documents);
        Collection<Index> indices = buildIndices(documents);
        insertIndices(indices);
        return indices;
    }


    @Override
    public Collection<DocumentDto> doSearchByWord(List<String> words, Page<Index> page) {
        String word = words.get(0);
        // Token -> Indices -> Documents
        Token          token     = tokenService.selectTokenByWord(word);
        List<Index>    indices   = selectIndicesByToken(token, page);
        List<Document> documents = documentService.selectDocumentsByIndices(indices);

        return convert2DocumentDto(word, documents, indices);
    }


    @Override
    public Collection<DocumentDto> doSearchBySentence(List<String> words, Page<Index> page) {
        Map<String, DocumentDto> hits = new HashMap<>(); // documentId : documentDto
        for (String word : words) {
            selectDocumentIndexedByWord(word, page, hits);
        }
        return hits.values();
    }


    @Override
    public Collection<DocumentDto> doSearchByQueryString(List<String> words, Page<Index> page) {
        Map<String, DocumentDto> hitDocumentDtoMap   = new HashMap<>(); // documentId : documentDto
        Set<String>              filteredDocumentIds = new HashSet<>();

        // search documents
        for (String word : words) {
            String         keyword = findKeyword(word);
            List<Document> documents;
            if (isFilteredWord(word)) {
                documents = selectDocumentIndexedByWord(keyword, page, null);
                documents.stream().map(Document::getId).forEach(filteredDocumentIds::add); // add filtered document id
            }
            else {
                selectDocumentIndexedByWord(keyword, page, hitDocumentDtoMap);
            }
        }

        return filterDocuments(hitDocumentDtoMap, filteredDocumentIds);
    }

    // ----------------------------------------------------------------------------------
    //                                  private method
    // ----------------------------------------------------------------------------------

    private Collection<Index> buildIndices(Collection<Document> documents) {
        ArrayList<Index> indices = new ArrayList<>();
        for (Document document : documents) {
            indices.addAll(buildIndices(document));
        }
        return indices;
    }

    /**
     * 生成 document 的倒排索引
     *
     * @param document 一篇文档
     * @return 生成的索引
     */
    private Collection<Index> buildIndices(Document document) {
        List<Index>          indices  = new ArrayList<>();
        List<String>         segments = segmentOnIndexMode(document.getText());
        Map<String, Keyword> keywords = analyze(document.getText(), segments.size());
        for (String segment : segments) {
            Token token = selectToken(segment);
            Index index = Index.of(token.getId(), document.getId());
            index.setScore(calcScore(keywords, token));
            index.setCount(1);
            indices.add(index);
        }
        return indices;
    }

    private Token selectToken(String segment) {
        Token token = tokenService.selectTokenByWord(segment);
        if (token == null) {
            token = tokenService.insert(segment);
        }
        return token;
    }

    private void insertIndices(Collection<Index> indices) {
        for (Index index : indices) {
            insertIndex(index);
        }
    }

    private void insertIndex(Index index) {
        Index existedIndex = indexMapper.selectOne(Factory.createLambdaQueryWrapper(Index.class)
                .eq(Index::getTokenId, index.getTokenId())
                .eq(Index::getDocumentId, index.getDocumentId()));
        if (existedIndex == null) {
            indexMapper.insert(index);
        }
        else {
            existedIndex.setCount(existedIndex.getCount() + 1);
            indexMapper.updateById(existedIndex);
        }
    }

    private List<Index> selectIndicesByToken(Token token, Page<Index> page) {
        if (token == null) {
            return new ArrayList<>(0);
        }
        return indexMapper.selectPage(Factory.resolvePage(page), Factory.createLambdaQueryWrapper(Index.class)
                .eq(Index::getTokenId, token.getId())
                .orderByDesc(Index::getScore)).getRecords();
    }

    private static List<DocumentDto> convert2DocumentDto(String word, List<Document> documents, List<Index> indices) {
        // documentId : Index
        Map<String, Index> indexes      = indices.stream().collect(Collectors.toMap(Index::getDocumentId, index -> index));
        List<DocumentDto>  documentDtos = new ArrayList<>();
        for (Document document : documents) {
            DocumentDto documentDto = DocumentDto.from(document);
            populateIndexInfoIfNecessary(word, documentDto, indexes.get(documentDto.getId()));
            documentDtos.add(documentDto);
        }
        return documentDtos;
    }

    /**
     * 处理某个单词
     * -csdn -> csdn
     * java -> java
     */
    private static String findKeyword(String word) {
        // "-csdn" indicates "csdn" is a filtered word
        return (isFilteredWord(word) ? word.substring(1) : word).toLowerCase(Locale.ENGLISH);
    }

    private static boolean isFilteredWord(String word) {
        return word.startsWith("-");
    }

    /**
     * 根据一个 word 查询对应的 document
     *
     * @param word 一个单词
     * @param page 分页
     * @param hits documentId : documentDto 如果这个哈希表被传入，将会把查出来的 document
     *             转为
     *             documentDto 并添加进哈希表
     * @return 查询出来的 document
     */
    private List<Document> selectDocumentIndexedByWord(String word, Page<Index> page,
                                                       Map<String, DocumentDto> hits) {
        // Token -> Index -> Document
        Token          token        = tokenService.selectTokenByWord(word);
        List<Index>    indices      = selectIndicesByToken(token, page);
        List<Document> newDocuments = documentService.selectDocumentsByIndices(indices);
        if (hits != null) {
            intersectDocuments(word, hits, indices, newDocuments);
        }
        return newDocuments;
    }

    /**
     * 对 hits 和 newDocuments 取交集
     *
     * @param word
     * @param hits
     * @param indices
     * @param newDocuments
     */
    private static void intersectDocuments(String word, Map<String, DocumentDto> hits,
                                           List<Index> indices, List<Document> newDocuments) {
        Map<String, Index> indexes = indices.stream()
                .collect(Collectors.toMap(Index::getDocumentId, index -> index)); // documentId : index

        // 把 document 转为 documentId 并取交集
        if (hits.isEmpty()) {
            // 第一次不用取交集，因为还是第一次还是空集
            for (Document document : newDocuments) {
                DocumentDto documentDto = DocumentDto.from(document);
                populateIndexInfoIfNecessary(word, documentDto, indexes.get(document.getId()));
                hits.put(document.getId(), documentDto);
            }
        }
        else {
            doIntersect(hits, newDocuments);
            for (DocumentDto documentDto : hits.values()) {
                populateIndexInfoIfNecessary(word, documentDto, indexes.get(documentDto.getId()));
            }
        }
    }

    private static void doIntersect(Map<String, DocumentDto> hitDocumentDtoMap, List<Document> newDocuments) {
        HashSet<String> newIds = newDocuments.stream().map(Document::getId)
                .collect(Collectors.toCollection(HashSet::new));
        hitDocumentDtoMap.keySet().removeIf(documentId -> !newIds.contains(documentId));
    }

    private static List<DocumentDto> filterDocuments(Map<String, DocumentDto> hits, Set<String> filteredDocumentIds) {
        return hits.values().stream()
                .filter(documentDto -> !filteredDocumentIds.contains(documentDto.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 向 documentDto 中添加命中的索引信息 index
     *
     * @param word        该索引的 tokenId 对应的 word
     * @param documentDto a documentDto
     * @param index       一个索引
     */
    private static void populateIndexInfoIfNecessary(String word, DocumentDto documentDto, Index index) {
        if (index != null) {
            populateIndexInfoForcefully(word, documentDto, index);
        }
    }

    private static void populateIndexInfoForcefully(String word, DocumentDto documentDto, Index index) {
        documentDto.addIndexInfo(word, IndexInfo.from(index));
    }
}