/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.pojo;

import com.github.howieyoung91.farseer.core.entity.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexInfo {
    private Integer count;
    private Double  score;

    public static IndexInfo from(Index index) {
        IndexInfo indexInfo = new IndexInfo();
        indexInfo.count = index.getCount();
        indexInfo.score = index.getScore();
        return indexInfo;
    }
}
