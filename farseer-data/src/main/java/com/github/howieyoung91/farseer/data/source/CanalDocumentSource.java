/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.data.source;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.github.howieyoung91.farseer.core.pojo.DocumentVo;
import com.github.howieyoung91.farseer.data.canal.Canal;
import com.github.howieyoung91.farseer.data.convert.DocumentVoConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 把 canal 封装为 DocumentSource
 *
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/08/13 16:07]
 */
@Component
@Slf4j
public class CanalDocumentSource extends AbstractDocumentSource<CanalEntry.RowChange> {
    @Autowired(required = false)
    private Canal canal;

    @Autowired(required = false)
    public CanalDocumentSource(DocumentVoConverter<CanalEntry.RowChange> converter) {
        super(converter);
    }

    @PostConstruct
    public void init() {
        canal.connect().subscribe();
    }

    @Override
    public Collection<DocumentVo> getDocuments() {
        ArrayList<DocumentVo>      result     = new ArrayList<>();
        List<CanalEntry.RowChange> rowChanges = canal.get();
        log.info("{}", rowChanges);
        if (rowChanges.isEmpty()) {
            return result;
        }
        DocumentVoConverter<CanalEntry.RowChange> converter = getDocumentConverter();
        for (CanalEntry.RowChange rowChange : rowChanges) {
            List<DocumentVo> documentVos = converter.convert(rowChange);
            result.addAll(documentVos);
        }
        return result;
    }
}
