/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.data.source;

import com.github.howieyoung91.farseer.core.pojo.DocumentVo;

import java.util.Collection;

public interface DocumentSource<S> {
    Collection<DocumentVo> getDocuments();
}
