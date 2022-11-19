/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.service;

import com.github.howieyoung91.farseer.core.service.index.support.*;
import com.github.howieyoung91.farseer.core.util.Redis;
import com.github.howieyoung91.farseer.core.word.SensitiveFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/11/18 18:10]
 */
@Configuration
public class IndexerService {
    @Resource
    DefaultIndexer  defaultIndexer;
    @Resource
    SensitiveFilter filter;
    @Resource
    Redis           redis;

    @Bean
    public SegmentCapableIndexer indexer() {
        return HighlightKeywordDecorator.decorate(
                SensitiveFilterDecorator.decorate(
                        CacheIndexDecorator.decorate(defaultIndexer, redis), filter));
    }
}
