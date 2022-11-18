/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.annotation;

import com.github.howieyoung91.farseer.core.entity.Document;

import java.util.List;

/**
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/11/17 13:08]
 */
public class NoConvertedDocumentsConverter implements Converter<List<Document>, List<Document>> {
    @Override
    public List<Document> convert(List<Document> source) {
        return source;
    }
}
