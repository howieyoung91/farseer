/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.pojo;

import lombok.Data;

@Data
public class DocumentVo {
    private String text;
    private String content;
    private String highlightPrefix;
    private String highlightSuffix;
}
