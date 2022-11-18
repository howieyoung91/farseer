/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.pojo;

import com.github.howieyoung91.farseer.core.entity.Document;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class DocumentDto {
    private String  id;
    private String  text;
    private String  content;
    private String  highlightPrefix;
    private String  highlightSuffix;
    private Details details = new Details();

    public static DocumentDto from(Document other) {
        DocumentDto self = new DocumentDto();
        self.setId(other.getId());
        self.setText(other.getText());
        self.setContent(other.getContent());
        self.setHighlightPrefix(other.getHighlightPrefix());
        self.setHighlightSuffix(other.getHighlightSuffix());
        return self;
    }

    public void addIndexInfo(String token, IndexInfo indexInfo) {
        details.hits.put(token, indexInfo);
    }

    @Data
    static class Details {
        Map<String, IndexInfo> hits = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DocumentDto that = (DocumentDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, content, details);
    }
}
