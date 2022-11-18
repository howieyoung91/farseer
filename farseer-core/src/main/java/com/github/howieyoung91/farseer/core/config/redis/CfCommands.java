/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.config.redis;

import io.lettuce.core.dynamic.Commands;
import io.lettuce.core.dynamic.annotation.Command;

import java.util.List;

public interface  CfCommands extends Commands {
    @Command("CF.ADD ?0 ?1")
    List<Object> add(String key, Object value);

    @Command("CF.DEL ?0 ?1")
    List<Object> del(String key, Object value);

    @Command("CF.EXISTS ?0 ?1")
    List<Object> exists(String key, Object value);
}
