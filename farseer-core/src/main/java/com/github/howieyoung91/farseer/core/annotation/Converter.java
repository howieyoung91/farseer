/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.annotation;

/**
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/11/17 13:07]
 */
public interface Converter<S, R> {
    R convert(S source);
}
