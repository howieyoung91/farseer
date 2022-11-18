/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Token implements Serializable {
    private String id;
    private String word;

    public static Token fromWord(String word) {
        Token token = new Token();
        token.setWord(word);
        return token;
    }
}
