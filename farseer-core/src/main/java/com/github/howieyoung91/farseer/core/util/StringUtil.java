/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.util;

public abstract class StringUtil {
    public static String[] splitByBlank(String s) {
        return s.split("\\s+");
    }
}
