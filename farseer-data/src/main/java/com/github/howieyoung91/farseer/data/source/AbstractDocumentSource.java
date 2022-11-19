/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.data.source;

import com.github.howieyoung91.farseer.data.convert.DocumentVoConverter;

public abstract class AbstractDocumentSource<S> implements DocumentSource<S> {
    private final DocumentVoConverter<S> converter;

    public AbstractDocumentSource(DocumentVoConverter<S> converter) {
        this.converter = converter;
    }

    protected DocumentVoConverter<S> getDocumentConverter() {
        return converter;
    }
}