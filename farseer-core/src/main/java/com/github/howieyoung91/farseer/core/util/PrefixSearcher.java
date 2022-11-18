/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.util;

import java.util.List;

public class PrefixSearcher {
    private final Trie trie = new Trie();
    private int count = 10;

    public PrefixSearcher() {
    }

    public PrefixSearcher(int count) {
        this.count = count;
    }

    public void addWords(String... words) {
        trie.addWords(words);
    }

    public List<String> searchByPrefix(String prefix) {
        return trie.getWords(prefix, count);
    }
}
