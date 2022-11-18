/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

public interface AbstractMapper<T> extends BaseMapper<T> {
    default Integer insertBatch(Collection<T> values) {
        return insertBatchSomeColumn(values);
    }

    Integer insertBatchSomeColumn(@Param("list") Collection<T> values);
}
